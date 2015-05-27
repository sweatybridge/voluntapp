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
    submitAjaxForm($(this), function(data) { toastr.success(data.responseJSON.message); }, $("#event_create_errors"));
  })

  // Bind calendar creation form
  $("#calendar_create_form").submit(function(e) {
    e.preventDefault()
    submitAjaxForm($(this), function(data) { toastr.success(data.name + " created!"); }, $("#calendar_create_errors"));
  });

  // Sets up request headers for all subsequent ajax calls
  $.ajaxSetup({
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader("Authorization", getCookie("token"))
    }
  });

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
    // shift weekday columns left by one
    $("#t_calendar tbody").children().each(function(k, v) {
      var first = $(v).children()[0];
      $(first).detach();
      $(first).appendTo(v);
    });
  });

  $("#next_day").click(function() {
    // shift weekday columns right by one
    $("#t_calendar tbody").children().each(function(k, v) {
      var last = $(v).children()[6];
      $(last).detach();
      $(last).prependTo(v);
    });
  });

  // Retrieve and render calendar events
  $.ajax("/json/calendar.json", {
    success: function(data) {
      $.each(data.events, function(index, value) {
        createEventView(value);
      });
    },
    error: function(data) {
      console.log("Failed to retrieve calendar events.");
    }
  });
}); // End of document ready

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

// Temporary function to get auth token from cookie
function getCookie(name) {
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
}

// Render a new event on the calendar
function createEventView(model) {
  // find the cell corresponding to start date
  $("#t_calendar_body").children().each(function(k, elem) {
    if ($(elem).data("date") === model.startDate) {
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
  $("#t_calendar_body").children().each(function(k, elem) {
    startDate.setDate(startDate.getDate() + 1);
    var date = startDate.toJSON().split("T")[0];
    $(elem).attr("data-date", date);
  });
}

// Get yesterday as date object
function yesterday() {
  var today = new Date();
  today.setDate(today.getDate() - 1);
  return today;
}

// Update user profile information on view
function refreshUser() {
  $.ajax("/api/user", {
    method: "GET",
    success: function(data) {
        $("[data-bind='email']").text(data.email);
        $("[data-bind='firstName']").text(data.firstName);
        $("[data-bind='lastName']").text(data.lastName);
        $("[data-bind='lastSeen']").text(data.lastSeen);
      }
  });
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
