// Document Ready
$(function() {
  // Bind calendar creation form
  $("#calendar_create_form").submit(function(e) {
    e.preventDefault();
    submitAjaxForm($(this), function(data) {
      toastr.success(data.name + " created!");
      $('#nav_create_tabs a:first').tab('show');
      refreshCalendars();
    }, $("#calendar_create_errors"));
  });

  // Bind calendar joining form
  $("#calendar_follow_form").submit(function(e) {
    e.preventDefault();
    submitAjaxForm($(this), function(data) {
      toastr.success("You joined " + data.name);
      $('#nav_create_tabs a:first').tab('show');
      refreshCalendars();
    }, $("#calendar_follow_errors"));
  });
  
  // Bind edit calendar buttons and forms
  $("#b_cancel_calendar").click(function() {
    $("#d_edit_calendar").addClass("hidden");
  });
  $("#b_edit_calendar").click(function(e) {
    if (!$(this).closest(".panel-heading").hasClass("collapsed")) {
      e.stopPropagation();
    }
    $("#d_edit_calendar").toggle();
  });

  // Bind delete calendar button
  $("#b_delete_calendar").click(function() {
    $("#d_user_calendars").children(".active").each(function(k, elem) {
      var calid = $(elem).data("calid");
      var name = getCalendarById(calid).name;
      if(confirm("Are you sure you want to delete "+name+"?")) {
        $.ajax("/api/calendar/"+calid, {
          method: "DELETE",
          success: function(data) {
            toastr.warning("Deleted " + name);
            refreshCalendars();
          },
          error: function(data) {
            toastr.error("Could not delete " + name);
            refreshCalendars();
          }
        });
      }
    });
  });

  // Bind user promotion form
  $("#user_promotion_form").submit(function(e) {
    e.preventDefault();
    var active_calendars = getActiveCalendarIds();
    if (active_calendars.length !== 1) {
      return;
    }
    var calid = active_calendars[0];
    var form = $(this);
    form.attr("action", "/api/subscription/calendar/"+calid);
    var formObj = getFormObj(form);
    // Handle delete case
    if (formObj.role == "remove") {
      if (confirm("Are you sure you want to remove "+formObj.targetUserEmail+"?")) {
        $(this).attr("method", "DELETE"); // Update method
      } else {
        return; // Cancel
      }
    }
    submitAjaxForm($(this), function(data) {
      toastr.success("Updated user");
      $("#b_cancel_calendar").click();
    }, $("#user_promotion_errors"));
  });
  
  $("#calendar_edit_form").submit(function(e) {
    e.preventDefault();
    var active_calendars = getActiveCalendarIds();
    if (active_calendars.length !== 1) {
      return;
    }
    var calid = active_calendars[0];
    $(this).attr("action", "/api/calendar/"+calid);
    submitAjaxForm($(this), function(data) {
      toastr.success("Updated calendar");
      $("#b_cancel_calendar").click();
      refreshCalendars();
    }, $("#calendar_edit_errors"));
  });

  $("#b_unsub_calendar").click(function() {
    // get the list of selected calendars
    $("#d_user_calendars").children(".active").each(function(k, elem) {
      var cal_div = $(elem);
      var cid = cal_div.data("calid");
      var calendar = getCalendarById(cid);
      if (calendar.role === "basic" || calendar.role === "editor") {
        $.ajax("/api/subscription/calendar/" + cid, {
          data: JSON.stringify({targetUserEmail : app.user.email}),
          method: "DELETE",
          success: function(data) {
            toastr.warning("Unsubscribed from " + calendar.name);
            refreshCalendars();
          },
          error: function(data) {
            toastr.error("Could not unscubscribe from " + calendar.name);
            refreshCalendars();
          }
        });
      }
    });
  });

  // Render calendar from monday
  app.current_start_date = getMonday();
  updateCalendarDates();
}); // End of document ready

// Update calendars
function refreshCalendars() {
  $.get("/api/subscription/calendar", function(data) {
    app.calendars = data.calendars;
    renderCalendars();
  });
}

function renderCalendars() {
  var cal_html =
      '<li role="presentation" data-calid="{{id}}">'+
        '<span class="badge progress-bar-warning notification hidden"></span>'+
        '<a href="#">{{name}}<span class="label label-primary join-code">{{joinCode}}</span></a>'+
      '</li>';

  var myCalendar = $("#d_user_calendars");
  // Clean current visible data
  myCalendar.html("It seems like you haven't joined to or created any calendars...");
  $("#select_calendar").empty();
  
  // Check if there is any calendars returned
  if (app.calendars.length < 1) {
    $('#nav_create_tabs a:last').tab('show');
    return;
  }
  
  // We got calendars, clear division to repopulate
  myCalendar.empty();
  app.events = [];
  // If there is any, create calendar elements
  $.each(app.calendars, function(index, calendar) {
    var code = calendar.joinEnabled ? calendar.joinCode : "private";
    var cal_div = $(cal_html
        .replace("{{id}}", calendar.calendarId)
        .replace("{{name}}", calendar.name)
        .replace("{{joinCode}}", code))
        .appendTo(myCalendar);
    if (calendar.role === "admin") {
      // cal_div.find(".calendar-unsub").toggle();
    }      

    cal_div.click(function() {
      cal_div.toggleClass("active").children().blur();
      if (calendar.role === "admin") {
        // cal_div.find(".calendar-extras").toggle();
      }
      if (cal_div.hasClass("active")) {
        getCalendarEventsByCid(calendar.calendarId);
        // TODO: wrap in async callback once calendar is mvc
        cal_div.find(".badge").addClass("hidden");
      } else {
        // remove events from view
        app.events = app.events.filter(function(e) {
          var isActive = (e.model.calendarId !== calendar.calendarId);
          if (!isActive) {
            e.view.remove();
          }
          return isActive;
        });
      }
      // update cookie
      setCookie("active_calendars", getActiveCalendarIds());
    });

    // Check calendar rights
    if (calendar.role === "editor" || calendar.role === "admin") {
      // Update event calendar selection box
      $('#select_calendar')
       .append($("<option></option>")
       .attr("value",calendar.calendarId)
       .text(calendar.name));
      /*
      cal_div.find("button").click(function() {
        var calid = $(this).parent().parent().data("calid");
        $("#d_edit_calendar input[name='name']").val(calendar.name);
        $("#d_edit_calendar input[type='checkbox']").prop("checked", calendar.joinEnabled);
        $("#d_user_calendars").toggle();
        $("#d_edit_calendar").data("calid", calid).toggle();
      });
      */
    }
  });

  // Refresh events for the calendars from cookie
  var active_calendars = getCookie("active_calendars");
  if (active_calendars) {
    myCalendar.children().each(function(k, elem) {
      var cal = $(elem);
      if (active_calendars.indexOf(cal.data("calid")) !== -1) {
        cal.click();
      }
    });
  } else {
    myCalendar.children().first().click();
  }
}

// Update data-date field of calendar view from startDate
function updateCalendarDates() {
  var startDate = new Date(app.current_start_date);
  $("#pickStartDate").datetimepicker({value: startDate});

  $("#prev_day").next().text(formatDate(startDate));
  var allDays = $("#t_calendar_body").children();

  var today = new Date();
  allDays.each(function(k, elem) {
    var current = new Date(startDate);
    current.setDate(current.getDate() + k);

    // update data fields
    var date = current.toLocaleDateString();
    $(elem).data("date", date);

    // update heading text
    var heading = $("#t_calendar_heading td:nth-child("+(k+1)+")");
    heading.text(getWeekDay(current) + " - " + formatDate(current));
    heading.removeClass("bg-primary").removeClass("th_weekend").removeClass("th_weekday");

    // highlight heading background
    if (date === today.toLocaleDateString()) {
      heading.addClass("bg-primary");
    }

    // update heading class
    var day = current.getDay();
    if (day === 0 || day === 6) {
      heading.addClass("th_weekend");
    } else {
      heading.addClass("th_weekday");
    }
  });

  startDate.setDate(startDate.getDate() + allDays.filter(":visible").length);
  $("#next_day").prev().text(formatDate(startDate));
}

// Get active_calendar ids
function getActiveCalendarIds() {
  var active_calendars = [];
  $("#d_user_calendars").children(".active").each(function(index, elem) {
    active_calendars.push($(elem).data("calid"));
  });
  return active_calendars;
}

// Increases notification badge count for a given calendarId if not enabled
// returns true if badge updated, false if the calendar is not enabled
function notifyBadge(calendarId) {
  // Update notification badge if calendar is not in selected view
  var active_calendars = getActiveCalendarIds();
  if (active_calendars.indexOf(calendarId) == -1) {
    // update notification badge
    $("#d_user_calendars").children().each(function(k, elem) {
      var view = $(elem);
      var cid = view.data("calid");
      if (cid == calendarId) {
        var notification = view.find(".badge");
        if (notification.hasClass("hidden")) {
          notification.removeClass("hidden").text("1");
        } else {
          var count = parseInt(notification.text());
          notification.text(count + 1);
        }
      }
    });
    return true;
  }
  return false;
}

// returns a fetched calendar by its id
function getCalendarById(cid) {
  return $.grep(app.calendars, function(e){ return e.calendarId === cid; })[0];
}
