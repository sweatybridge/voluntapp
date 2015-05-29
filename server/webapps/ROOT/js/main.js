var app = {joined:{}};

// DOCUMENT READY
$(function() {
  // Render calendar from yesterday
  updateCalendarDates(yesterday());

  // Bind refresh button
  $("#b_refresh").click(refreshEvents);
  
  // Bind weekend collapse
  $("#b_hide_weekend").click(function(){
    // TODO: check if this train reck is the only way to do this
    var sat_index = $('#t_calendar_heading th:contains("Sat")').index()+1;
    console.log(sat_index);
    var selector = "#t_calendar th:nth-of-type("+sat_index+"), #t_calendar td:nth-of-type("+sat_index+"), #t_calendar th:nth-of-type("+(sat_index+1)+"), #t_calendar td:nth-of-type("+(sat_index+1)+")";
    $(this).parent().toggleClass("active");
    $(selector).toggle();
  });

  // Bind sidebar collapse
  $("#b_hide_left").click(function() {
    $("#d_left_sidebar").toggleClass("col-hidden col-sm-2");
    $(this).parent().toggleClass("active");
    updateMainCol();
  });
  
  $("#b_hide_right").click(function() {
    $("#d_right_sidebar").toggleClass("col-hidden col-sm-2");
    $(this).parent().toggleClass("active");
    updateMainCol();        
  });
  
  // Bind logout button
  $("#b_logout").click(function() {
    $.ajax("/api/session", {
      method: "DELETE",
      success: function(data) { window.location.reload(); },
      error: function(data) { alert(data.responseJSON.message); }
    });
  });
  
  // Bind datetime picker
  $(".datetimepicker").datetimepicker();

  // Bind event creation form
  $("#event_form").submit(function(e) {
    e.preventDefault();
    // Seperate datetime into date and time
    if ($("#event_form select").val() < 0) {
      $("#event_create_errors").text("You must create a calendar first.");
      return;
    }
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
        refreshEvents();
      },
      error: function(data) { $("#event_create_errors").text(data.responseJSON.message); }
    });
  });

  // Bind calendar creation form
  $("#calendar_create_form").submit(function(e) {
    e.preventDefault()
    submitAjaxForm($(this), function(data) { toastr.success(data.name + " created!"); refreshCalendars(); }, $("#calendar_create_errors"));
  });
  
  // Bind calendar joining form
  $("#calendar_follow_form").submit(function(e) {
    e.preventDefault()
    submitAjaxForm($(this), function(data) { toastr.success("You started following " + data.name); refreshCalendars(); }, $("#calendar_follow_errors"));
  });

  // Sets up request headers for all subsequent ajax calls
  $.ajaxSetup({
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader("Authorization", getCookie("token"));
    }
  });
  
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
    // advance date by 1
    app.current_start_date.setDate(app.current_start_date.getDate() - 1);
    refreshEvents();
  });

  $("#next_day").click(function() {
    // shift weekday columns right by one
    app.current_start_date.setDate(app.current_start_date.getDate() + 1);
    refreshEvents();
  });
  
  /*toastr.options = {
    "progressBar": false,
    "positionClass": "toast-bottom-center",
    "onclick": null
  }*/
  
  // Request user profile information
  refreshUser();
  
  // Request calendar information
  refreshCalendars();
}); // End of document ready

// Update user profile information on view
function refreshUser() {
  $.get("/api/user",
    function(data) {
      app.user = data;
      $("[data-bind='email']").text(data.email);
      $("[data-bind='firstName']").text(data.firstName);
      $("[data-bind='lastName']").text(data.lastName);
      $("[data-bind='lastSeen']").text(data.lastSeen);
  });
}

// Update calendars
function refreshCalendars() {
  $.get("/api/subscription/calendar", function(data) {
    app.calendars = data.calendars;
    if (data.calendars.length < 1) {
      return;
    }
    $("#user_calendars").empty();
    $("#select_calendar").empty();
    $.each(data.calendars, function(index, calendar) {
      $('#select_calendar')
       .append($("<option></option>")
       .attr("value",calendar.calendarId)
       .text(calendar.name));
       var checkbox = $("<input>").attr("type", "checkbox").attr("data-calid", calendar.calendarId);
       // Bind event rendering
       checkbox.change(function() { refreshEvents(); });
      $("<div>").addClass("checkbox").append($("<label>").append(checkbox).append(calendar.name + ' - ' + calendar.joinCode)).appendTo("#user_calendars");
    });
    // Refresh events for the calendars
    refreshEvents();
  });
}

// Update Events
function refreshEvents() {
  // Retrieve and render calendar events
  app.events = [];
  var active_calendars = getActiveCalendarIds();
  // Just re-render if there are no active calendars
  if (active_calendars.length < 1) {
    updateCalendarDates(app.current_start_date);
    renderEvents();
  }
  // Get event data for the active calendars then render
  $.each(active_calendars, function(index, id) {
    $.ajax("/api/calendar/"+id, {
      data: {startDate: app.current_start_date.toJSON().split('T')[0] + " 00:00:00"},
      success: function(data) {
        // Add the calendarId because back-end doesn't provide it
        $.each(data.events, function(index, event) {
          event.calendarId = id;
        });
        app.events.push.apply(app.events, data.events);
        updateCalendarDates(app.current_start_date);
        renderEvents();
      },
      error: function(data) {
        toastr.error("Failed to get events for " + id);
      }
    });
  });
  
}

// Render events
function renderEvents() {
  var active_calendars = getActiveCalendarIds();
  // Clear any existing events
  $("#t_calendar_body").children().each(function(index) {
    $(this).empty();
  });
  
  // Rerender active calendars' events
  $.each(app.events, function(index, event) {
    if (active_calendars.indexOf(event.calendarId) >= 0) {
      createEventView(event);
    }
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
function createEventView(event) {
  // expand description on hover
  // find the cell corresponding to start date
  var temp =
  '<div data-event-id="{{eventId}}" class="event">'+
    '<div class="time">'+
      '<dd>{{startDate}}</dd>'+
      '<dd>{{startTime}}</dd>'+
    '</div>'+
    '<div class="header">'+
      '<span class="label label-warning count">{{remaining}}</span>'+
    '</div>'+
    '<div class="title">{{title}}</div>'+
    '<div class="desc">{{description}}</div>'+
    '<div class="location">'+
      '<span class="glyphicon glyphicon-map-marker"></span> {{location}}'+
    '</div>'+
    '<div class="join">'+
      '<a class="badge" onclick=joinEvent(this)>Join</a>'+
    '</div>'+
  '</div>';
  $("#t_calendar_body").children().each(function(k, elem) {
    if ($(elem).data("date") === event.startDate) {
      var startDateTime = new Date(event.startDate + "T" + event.startTime.split("+")[0]);
      var readableDate = formatDate(startDateTime).split(" ").reverse().join(" ");
      var readableTime = startDateTime.toLocaleTimeString().substring(0, 5);
      // append event div
      temp = temp
        .replace('{{eventId}}', event.eventId)
        .replace('{{startDate}}', readableDate)
        .replace('{{startTime}}', readableTime)
        .replace('{{title}}', event.title)
        .replace('{{description}}', event.description)
        .replace('{{location}}', event.location);
      
      if (event.max == -1) {
        temp = temp.replace('{{remaining}}', "&infin;");
      } else {
        temp = temp.replace('{{remaining}}', event.max - event.currentCount);
      }
      var view = $(temp);
      $(elem).append(view);
      if (event.hasJoined) {
        // update joined badge
        view.find(".badge").addClass("progress-bar-success").text("Joined");
      } else if (event.max - event.currentCount == 0) {
        view.find(".badge").css("visibility", "hidden");
      }
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
    $(elem).data("date", date);

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

// Get active_calendar ids
function getActiveCalendarIds() {
  var active_calendars = [];
  $("#calendars_collapse input").each(function(index) {
    if ($(this).is(":checked")) {
      active_calendars.push($(this).data("calid"));
    }
  });
  return active_calendars;
}

// Join an event
function joinEvent(elem) {
  var view = $(elem).closest(".event");
  var eid = view.data("eventId");
  var event = $.grep(app.events, function(e){ return e.eventId == eid; })[0];
  
  // determine wether to join or unjoin
  if (event.hasJoined) {
    // unjoin an event
    $.ajax("/api/subscription/event", {
      method: "DELETE",
      data: JSON.stringify({eventId: eid}),
      success: function(data) {
        event.hasJoined = false;
        toastr.warning("Unjoined event " + event.title);
        if (event.max > -1) {
          // update remaining spots
          event.currentCount -= 1;
          view.find(".count").text(event.max - event.currentCount);
        }
        // update badge
        $(elem).removeClass("progress-bar-success").text("Join");
      },
      error: function(data) {
        toastr.error("Cannot join event: " + data.responseJSON.message);
        refreshEvents();
      }
    });
  } else {
    // join an event if there are spaces left
    if ( event.max > 0 && event.max - event.currentCount < 1) {
      toastr.error(event.title + " is full");
      return;
    }
    if (event.max == 0) {
      toastr.error("Joining for " + event.title + " is disabled");
      return;
    }
    $.ajax("/api/subscription/event", {
      method: "POST",
      data: JSON.stringify({eventId: eid}),
      success: function(data) {
        toastr.success("Joined event " + event.title);
        // use dictionary to prevent duplicates
        event.hasJoined = true;
        // update remaining spots
        event.currentCount += 1;
        if (event.max > -1) {
          view.find(".count").text(event.max - event.currentCount);
        }
        // update badge
        $(elem).addClass("progress-bar-success").text("Joined");
      },
      error: function(data) {
        toastr.error("Cannot join event: " + data.responseJSON.message);
        refreshEvents();
      }
    });
  }
}
