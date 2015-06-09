// Document Ready
$(function() {
  // Bind calendar creation form
  $("#calendar_create_form").submit(function(e) {
    e.preventDefault()
    submitAjaxForm($(this), function(data) { toastr.success(data.name + " created!"); $('#nav_create_tabs a:first').tab('show'); refreshCalendars(); }, $("#calendar_create_errors"));
  });
  
  // Bind calendar joining form
  $("#calendar_follow_form").submit(function(e) {
    e.preventDefault()
    submitAjaxForm($(this), function(data) { toastr.success("You started following " + data.name); $('#nav_create_tabs a:first').tab('show'); refreshCalendars(); }, $("#calendar_follow_errors"));
  });
  
  // Bind edit calendar buttons and forms
  $("#b_cancel_calendar").click(function() {
    $("#d_edit_calendar").toggle();
    $("#d_user_calendars").toggle();
  });
  
  $("#b_delete_calendar").click(function() {
    var calid = $(this).parent().data("calid");
    var name = $.grep(app.calendars, function(e){ return e.calendarId == calid; })[0].name;
    if(confirm("Are you sure you want to delete "+name+"?")) {
      $.ajax("/api/calendar/"+calid, {
        method: "DELETE",
        success: function(data) {
          toastr.warning("Deleted " + name);
          $("#b_cancel_calendar").click(); // hide this window again
          refreshCalendars();
        },
        error: function(data) {
          toastr.error("Could not delete " + name);
          refreshCalendars();
        }
      });
    }
  });
  
  $("#user_promotion_form").submit(function(e) {
    e.preventDefault();
    var calid = $(this).parent().data("calid");
    $(this).attr("action", "/api/subscription/calendar/"+calid);
    var formObj = getFormObj($(this));
    console.log(formObj);
    // Handle delete case
    if (formObj.role == "remove") {
      if (confirm("Are you sure you want to remove "+formObj.targetUserEmail+"?")) {
        $(this).attr("method", "DELETE"); // Update method
      } else {
        return; // Cancel
      }
    }
    submitAjaxForm($(this), function(data) { toastr.success("Updated user"); $("#b_cancel_calendar").click(); }, $("#user_promotion_errors"));
  });
  
  $("#calendar_edit_form").submit(function(e) {
    e.preventDefault();
    var calid = $(this).parent().data("calid");
    $(this).attr("action", "/api/calendar/"+calid);
    submitAjaxForm($(this), function(data) { toastr.success("Updated calendar"); $("#b_cancel_calendar").click(); refreshCalendars(); }, $("#calendar_edit_errors"));
  });
  
  // Render calendar from yesterday
  // updateCalendarDates(getMonday());
  app.current_start_date = getMonday();

  //rebuildCalendar();
}); // End of document ready

// Update calendars
function refreshCalendars() {
  var cal_html =
      '<li role="presentation" data-calid="{{id}}">'+
        '<a href="#">{{name}}<span class="label label-default pull-right">{{joinCode}}</span></a>'+
      '</li>';
/*
'<div data-calid="{{id}}" class="calendar"> \
  <div class="checkbox"> \
    <span class="subcheck"> \
      <input type="checkbox"> {{name}}   \
    </label> \
    </span> \
    <a href="#" class="calendar-unsub"><span class="remove-sub glyphicon glyphicon-minus scarlet"></span></a> \
  </div> \
  <div class="calendar-extras" style="display: none;"> \
    <p>Join code: <strong>{{joinCode}}</strong></p> \
    <p>Join enabled: <strong>{{joinEnabled}}</strong></p> \
    <button type="button" class="btn btn-info">Edit</button> \
  </div> \
</div>';
*/
  $.get("/api/subscription/calendar", function(data) {
    app.calendars = data.calendars;
    var myCalendar = $("#d_user_calendars");
    // Clean current visible data
    myCalendar.html("It seems like you haven't joined to or created any calendars...");
    $("#select_calendar").empty();
    
    // Check if there is any calendars returned
    if (data.calendars.length < 1) {
      $('#nav_create_tabs a:last').tab('show');
      return;
    }
    
    // We got calendars, clear division to repopulate
    myCalendar.empty();
    // If there is any, create calendar elements
    $.each(data.calendars, function(index, calendar) {
      var code = calendar.joinEnabled ? calendar.joinCode : "disabled";
      var cal_div = $(cal_html
          .replace("{{id}}", calendar.calendarId)
          .replace("{{name}}", calendar.name)
          .replace("{{joinCode}}", code))
          .appendTo(myCalendar);
      if (calendar.role === "admin" || calendar.role === "owner") {
        // cal_div.find(".calendar-unsub").toggle();
      }      

      cal_div.click(function() {
        cal_div.toggleClass("active").children().blur();
        if (calendar.role === "admin" || calendar.role === "owner") {
          // cal_div.find(".calendar-extras").toggle();
        }
        refreshEvents();
      });

      // Add unsubscribing from events
      /*
      if (calendar.role === "basic" || calendar.role === "editor") {
  		  cal_div.find("a").click(function() {
  		    var calid = $(this).parent().parent().data("calid");
  		    var name = $.grep(app.calendars, function(e){ return e.calendarId == calid; })[0].name;
  		    $.ajax("/api/subscription/calendar/"+calid, {
  				data: JSON.stringify({targetUserEmail : app.user.email}),
  				method: "DELETE",
  				success: function(data) {
  				  toastr.warning("Unsubscribed from " + name);
  				  refreshCalendars();
  				},
  				error: function(data) {
  				  toastr.error("Could not unscubscribe from " + name);
  				  refreshCalendars();
  				}
  		    });
  		  });
      }
      */
      
      // Check calendar rights
      if (calendar.role === "admin" || calendar.role === "owner") {
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
    // Refresh events for the calendars
    $("#d_user_calendars").children().first().click();
  });
}

// Update data-date field of calendar view from startDate
function updateCalendarDates(startDate) {
  var today = new Date();
  app.current_start_date = new Date(startDate);
  $("#pickStartDate").datetimepicker({value: app.current_start_date});

  $("#prev_day").next().text(formatDate(startDate));

  $("#t_calendar_body").children(":visible").each(function(k, elem) {
    // update data fields
    var date = startDate.toLocaleDateString();
    $(elem).data("date", date);

    // update heading text
    var heading = $("#t_calendar_heading td:nth-child("+(k+1)+")");
    heading.text(getWeekDay(startDate) + " - " + formatDate(startDate));
    heading.removeClass("bg-primary").removeClass("th_weekend").removeClass("th_weekday");

    // highlight heading background
    if (date === today.toLocaleDateString()) {
      heading.addClass("bg-primary");
    }

    // update heading class
    var day = startDate.getDay();
    if (day === 0 || day === 6) {
      heading.addClass("th_weekend");
    } else {
      heading.addClass("th_weekday");
    }

    // increment date
    startDate.setDate(startDate.getDate() + 1);
  });

  startDate.setDate(startDate.getDate() - 1);
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
