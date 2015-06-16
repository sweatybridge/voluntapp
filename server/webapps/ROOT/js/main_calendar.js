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
        '<a href="#">{{name}}'+
          '<span class="calendar-badges">'+
            '<span class="label label-warning join-code">{{role}}</span>'+
            '<span class="label label-primary join-code">{{joinCode}}</span>'+
          '</span>'+
        '</a>'+
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
  $("#t_calendar_body").children().empty();

  // If there is any, create calendar elements
  $.each(app.calendars, function(index, calendar) {
    var code = calendar.joinEnabled ? calendar.joinCode : "private";
    var cal_div = $(cal_html
        .replace("{{id}}", calendar.calendarId)
        .replace("{{name}}", calendar.name)
        .replace("{{role}}", calendar.role.charAt(0).toUpperCase())
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

  var allDays = $("#t_calendar_body").children();
  var today = new Date();
  var current = new Date(startDate);
  var last = allDays.filter(":visible").length - 1;
  allDays.each(function(k, elem) {
    // check if hide weekend
    var day = current.getDay();
    if (app.hide_weekend) {
      if (day === 0) {
        current.setDate(current.getDate() + 1);
      } else if (day === 6) {
        current.setDate(current.getDate() + 2);
      }
    }

    // update first day
    if (k === 0) {
      $("#prev_day").next().text(formatDate(current));
    }

    // update data fields
    var date = current.toLocaleDateString();
    $(elem).data("date", date);

    // update heading text
    var heading = $("#t_calendar_heading td:nth-child("+(k+1)+")");
    heading.text(getWeekDay(current) + " - " + formatDate(current));

    // highlight heading background
    if (date === today.toLocaleDateString()) {
      heading.addClass("bg-primary");
    } else {
      heading.removeClass("bg-primary");
    }

    // update last day
    if (k === last) {
      $("#next_day").prev().text(formatDate(current));
    }

    current.setDate(current.getDate() + 1);
  });
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
  return $.grep(app.calendars, function(c){ return c.calendarId === cid; })[0];
}
