var app = {};

// DOCUMENT READY
$(function() {
  // Render calendar from yesterday
  updateCalendarDates(yesterday());

  // Bind weekend collapse
  $("#b_hide_weekend").click(function(){
    // TODO: check if this train reck is the only way to do this
    $("#t_calendar th:nth-of-type(6), #t_calendar td:nth-of-type(6), #t_calendar th:nth-of-type(7), #t_calendar td:nth-of-type(7)").toggle();
  });

  // Bind sidebar collapse
  $("#b_hide_left").click(function() {
    $("#d_left_sidebar").toggleClass("col-hidden col-sm-2");
    updateMainCol();
  });
  
  $("#b_hide_right").click(function() {
    $("#d_right_sidebar").toggleClass("col-hidden col-sm-2");
    updateMainCol();        
  });
  
  // Bind event description show button
  $(".event button").click(function() {
    $(this).next(".e_desc").toggle(500);
  });
  
  // Bind datetime picker
  $(".datetimepicker").datetimepicker();

  // Bind logout button
  $("#btn_logout").click(function() {
    $.ajax("/api/session", {
      method: "DELETE",
      success: function(data) { window.location.reload(); },
      error: function(data) { alert(data.responseJSON.message); }
    });
  });

  // Bind event creation form
  $("#event_form").submit(function(e) {
    e.preventDefault();
    // Seperate datetime into date and time
    var form = $(this);
    var formObj = getFormObj(form);
    var regex = new RegExp('/', "g");
    formObj["startTime"] = formObj["startDate"].split(" ")[1];
    formObj["startDate"] = formObj["startDate"].split(" ")[0].replace(regex, '-');
    formObj["endTime"] = formObj["endDate"].split(" ")[1];
    formObj["endDate"] = formObj["endDate"].split(" ")[0].replace(regex, '-');
    console.log(formObj);
    formObj["timezone"] = jstz.determine().name();
    
    // Submit form
    $.ajax(form.attr("action"), {
      data: JSON.stringify(formObj),
      contentType: "application/json; charset=utf-8",
      dataType: "json",
      method: form.attr("method"),
      success: function(data) {
        toastr.success("Created " + formObj["title"]);
        app.events.push(formObj);
        createEventView(formObj);
      },
      error: function(data) { $("#event_create_errors").text(data.responseJSON.message); }
    });
  });

  // Bind calendar creation form
  $("#calendar_create_form").submit(function(e) {
    e.preventDefault()
    submitAjaxForm($(this), function(data) { toastr.success(data.name + " created!"); refreshCalendar(); }, $("#calendar_create_errors"));
  });
  
  // Bind calendar joining form
  $("#calendar_follow_form").submit(function(e) {
    e.preventDefault()
    submitAjaxForm($(this), function(data) { toastr.success("You started following " + data.name); refreshCalendar(); }, $("#calendar_follow_errors"));
  });

  // Sets up request headers for all subsequent ajax calls
  $.ajaxSetup({
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader("Authorization", getCookie("token"));
    }
  });
  
  // Request calendar information
  refreshCalendar();

  // Request user profile information
  refreshUser();
  
  // Bind user profile buttons
  $("#profile_form").hide();
  $("#b_update_profile").click(function() {
    $("#d_user_profile").toggle();
    $("#profile_form").toggle();
  });
  $("#b_cancel_profile").click(function() {
    $("#profile_form").toggle();
    $("#d_user_profile").toggle();
  });
  
  // Bind user profile update form
  // Bind calendar creation form
  $("#profile_form").submit(function(e) {
    e.preventDefault();
    var form = $(this);
    
    // Validate
    if (validateUpdate(form)) {
      return;
    }
    submitAjaxForm(form, function(data) { toastr.success(data.message); }, $("#profile_errors"));
  });

  // Bind previous and next day button
  $("#prev_day").click(function() {
    // TODO: Retrieve more events from server
    
    // get tomorrow's events from local storage
    
    // advance date by 1
    app.current_start_date.setDate(app.current_start_date.getDate() - 1);
    updateCalendarDates(app.current_start_date);
    renderEvents();
  });

  $("#next_day").click(function() {
    // TODO retrieve more events from server

    // get yesterday's events from local storage

    // shift weekday columns right by one
    app.current_start_date.setDate(app.current_start_date.getDate() + 1);
    updateCalendarDates(app.current_start_date);
    renderEvents();
  });

  // Retrieve and render calendar events
  $.ajax("/json/events.json", {
    success: function(data) {
      app.events = data;
      $.each(app.events, function(index, event) {
        createEventView(event);
      });
    },
    error: function(data) {
      console.log("Failed to retrieve calendar events.");
    }
  });
}); // End of document ready

// Update user profile information on view
function refreshUser() {
  $.get("/api/user",
    function(data) {
      $("[data-bind='email']").text(data.email);
      $("[data-bind='firstName']").text(data.firstName);
      $("[data-bind='lastName']").text(data.lastName);
      $("[data-bind='lastSeen']").text(data.lastSeen);
  });
}

// Update calendars
function refreshCalendar() {
  $.get("/json/calendar.json", function(data) {
    if (data.length < 1) {
      return;
    }
    $("#user_calendars").empty();
    $("#select_calendar").empty();
    $.each(data, function(index, calendar) {
      // TODO: update global variable with calendar data
      app.calendars = data;
      $('#select_calendar')
       .append($("<option></option>")
       .attr("value",calendar.calendarId)
       .text(calendar.name));
      $("<div>").addClass("checkbox").append($("<label>").html('<input type="checkbox" checked>'+calendar.name)).appendTo("#user_calendars");
    });
  });
}

// Update Events
function refreshEvents() {
  
}

// Render events
function renderEvents() {
  $.each(app.events, function(index, event) {
    createEventView(event);
  });
}

// Update main column class whether sizebars are hidden or not
function updateMainCol() {
  var size = 8;
  if ($("#d_right_sidebar").hasClass("col-hidden")) {
    size += 2;
  }
  if ($("#d_left_sidebar").hasClass("col-hidden")) {
    size += 2;
  }
  $("#d_main_col").attr("class", "col-sm-" + size);
}

// Render a new event on the calendar
function createEventView(model) {
  // find the cell corresponding to start date
  $("#t_calendar_body").children().each(function(k, elem) {
    if ($(elem).attr("data-date") === model.startDate) {
      // append event div
      $("<div/>", {
        "data-eventId": model.eventId,
        class: "event"
      }).append($("<p/>", {
        class: "e_title",
        text: model.title
      })).append($("<span/>", {
        class: "e_time",
        text: model.startTime
      })).append($("<p/>", {
        class: "e_desc",
        text: model.description
      })).appendTo(elem);
    }
    // if event not in view, don't render
  });
}

// Update data-date field of calendar view from startDate
function updateCalendarDates(startDate) {
  // TODO: Handle different time zone
  app.current_start_date = new Date(startDate);
  $("#prev_day").next().text(formatDate(startDate));

  $("#t_calendar_body").children().each(function(k, elem) {
    // update data fields
    var date = startDate.toJSON().split("T")[0];
    $(elem).attr("data-date", date);

    // update heading text
    var heading = $($("#t_calendar_heading").children()[k]);
    heading.text(getWeekDay(startDate));

    // update heading class
    heading.removeClass("th_weekend").removeClass("th_weekday");
    var day = startDate.getDay();
    if (day === 0 || day === 6) {
      heading.addClass("th_weekend");
    } else {
      heading.addClass("th_weekday");
    }

    // clear all current events visible on this day
    $(elem).empty();

    // increment date
    startDate.setDate(startDate.getDate() + 1);
  });

  $("#next_day").prev().text(formatDate(startDate));
}

// Validation of update form
function validateUpdate($form) {
  var form = $form[0];
  var pass_val = form["newPassword"].value;
  
  // Check password length
  if (pass_val.length < 6) {
    $("#profile_errors").text("New password must be at least 6 characters long");
    return true;
  }
  
  // Check confirmation
  var conf_pass_val = form["confPassword"].value;
  if (pass_val !== conf_pass_val) {
    $("#profile_errors").text("Password must match");
    return true;
  }
  return false;
}
