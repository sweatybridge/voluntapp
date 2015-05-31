// Document Ready
$(function() {
  // Bind datetime picker
  // http://xdsoft.net/jqplugins/datetimepicker/
  $(".datetimepicker").datetimepicker({
    format: "Y-m-d H:i"
  });

  // Bind edit event buttons
  $("#btn_event_save").click(function() {
    // Retrieve form data
    var form = $("#event_form");
    var formObj = getFormObj(form);
    if (!formObj.eventId) {
      toastr.error("Failed to read event id. Please select the event again or refresh the app.");
      return;
    }

    // validate form
    formatEventForm(formObj);
    // ajax put
    $.ajax(form.attr("action") +"/"+formObj.eventId, {
      method: "PUT",
      data: JSON.stringify(formObj),
      success: function(data) {
        toastr.success("Saved chanages to " + formObj["title"]);
        refreshEvents();
        form.trigger("reset");
        turnEventCreate();
      },
      error: function(data) { $("#event_create_errors").text(data.responseJSON.message); }
    })
  });

  $("#btn_event_cancel").click(function() {
    $("#event_form").trigger("reset");
    turnEventCreate();
  });

  // delete event
  $("#btn_event_delete").click(function() {
    // Retrieve form data
    var form = $("#event_form");
    var formObj = getFormObj(form);
    if (!formObj.eventId) {
      toastr.error("Failed to read event id. Please select the event again or refresh the app.");
      return;
    }
    
    // User confirmation
    if(!confirm("Are you sure you want to delete "+formObj.title+"?")) {
      return
    }

    // ajax delete
    $.ajax(form.attr("action") +"/"+formObj.eventId, {
      method: "DELETE",
      success: function(data) {
        toastr.success("Deleted event " + formObj["title"]);
        refreshEvents();
        form.trigger("reset");
        turnEventCreate();
      },
      error: function(data) { $("#event_create_errors").text(data.responseJSON.message); }
    });
  });

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
    formatEventForm(formObj);
    

    // Check for the description length
    if (formObj["description"].length > 255) {
      $("#event_create_errors").text("Description length is too long.");
      return;
    }
    
    // Submit form
    $.ajax(form.attr("action"), {
      data: JSON.stringify(formObj),
      method: form.attr("method"),
      success: function(data) {
        toastr.success("Created " + formObj["title"]);
        refreshEvents();
        form.trigger("reset");
      },
      error: function(data) { $("#event_create_errors").text(data.responseJSON.message); }
    });
  });
  
  // bind click to empty space on calendar
  $("#t_calendar_body").children().click(function() {
    // update create event form
    var start = $(this).data("date")
    $("#event_form").trigger("reset").find('input[name="startDate"]').val(start);
    turnEventCreate();
  });
  
  // Refresh description count
  $('#event_form textarea[name="description"]').change(updateCountdown);
  $('#event_form textarea[name="description"]').keyup(updateCountdown);
  updateCountdown();

}); // End of document ready

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
  
  // Sort events because back-end doesn't
  // TODO: check sorting
  app.events.sort(function(a,b) {
    // Something like
    var aDate = new Date(a.startDate + ' ' + a.startTime);
    var bDate = new Date(b.startDate + ' ' + b.startTime);
  });
  // Rerender active calendars' events
  $.each(app.events, function(index, event) {
    if (active_calendars.indexOf(event.calendarId) >= 0) {
      createEventView(event);
    }
  });
}

// Render a new event on the calendar
function createEventView(event) {
  // expand description on hover
  // find the cell corresponding to start date
  var temp =
  '<div data-event-id="{{eventId}}" class="event" onclick="event.stopPropagation();">'+
    '<div class="time">'+
      '<dd>{{startDate}}</dd>'+
      '<dd>{{startTime}}</dd>'+
    '</div>'+
    '<div class="header progress-bar-info" onclick="editEvent(this)">'+
      '<span class="label label-warning count">{{remaining}}</span>'+
    '</div>'+
    '<div class="title">{{title}}</div>'+
    '<div class="event-extras"><div class="desc">{{description}}</div><div class="requirements"></div></div>'+
    '<div class="location">'+
      '<span class="glyphicon glyphicon-map-marker"></span> {{location}}'+
    '</div>'+
    '<div class="join">'+
      '<a class="badge" onclick="joinEvent(this)">Join</a>'+
    '</div>'+
  '</div>';
  $("#t_calendar_body").children().each(function(k, elem) {
    var start = new Date(event.startDateTime);
    if ($(elem).data("date") === start.toLocaleDateString()) {
      var readableDate = formatDate(start).split(" ").reverse().join(" ");
      var readableTime = start.toLocaleTimeString().substring(0, 5);
      
      // Extract requirements from description
      var lines = event.description.split("\n");
      var requirements = [];
      var description = [];
      lines.forEach(function(l) {
        console.log(l);
        if (startsWith(l, "R-")) {
          requirements.push(l.slice(2));
        } else {
          description.push(l);
        }
      });
      
      // TODO: Maybe remove trailing new lines from the description?
      // as in ["", "desc"] or ["desc", ""]
      description = description.join("\n");
      
      console.log(requirements);
      console.log(description);
      // append event div
      temp = temp
        .replace('{{eventId}}', event.eventId)
        .replace('{{startDate}}', readableDate)
        .replace('{{startTime}}', readableTime)
        .replace('{{title}}', event.title)
        .replace('{{description}}', description)
        .replace('{{location}}', event.location);
      
      if (event.max == -1) {
        temp = temp.replace('{{remaining}}', "&infin;");
      } else {
        temp = temp.replace('{{remaining}}', event.currentCount + "/" + event.max);
      }
      
      var view = $(temp);
      
      // Add in the requirement checkboxes
      var req_html = '<div class="checkbox"> \
                      <label> \
                        <input type="checkbox"> {{requirement}} \
                      </label> \
                    </div>';
      requirements.forEach(function(r) {
        var req_checkbox = $(req_html.replace("{{requirement}}", r));
        view.find(".requirements").append(req_checkbox);
      });
      
      // Append the view to the actual td
      $(elem).append(view);
      if (event.hasJoined) {
        // update joined badge
        view.find(".badge").addClass("progress-bar-danger").text("Unjoin");
        // update header
        view.find(".header").removeClass("progress-bar-info").addClass("progress-bar-success");
      } else if (event.max - event.currentCount == 0) {
        view.find(".badge").css("visibility", "hidden");
      }
      // hide location if it is not set
      if (!event.location) {
        view.find(".location").hide();
      }
    }
    // if event not in view, don't render
  });
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
          view.find(".count").text(event.currentCount + "/" + event.max);
        }
        // update badge
        $(elem).removeClass("progress-bar-danger").text("Join");
        // update header
        view.find(".header").removeClass("progress-bar-success").addClass("progress-bar-info");
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
          view.find(".count").text(event.currentCount + "/" + event.max);
        }
        // update badge
        $(elem).addClass("progress-bar-danger").text("Unjoin");
        // update header
        view.find(".header").removeClass("progress-bar-info").addClass("progress-bar-success");
      },
      error: function(data) {
        toastr.error("Cannot unjoin event: " + data.responseJSON.message);
        refreshEvents();
      }
    });
  }
}

// Handler for editing event
function editEvent(elem) {
  var view = $(elem).closest(".event");
  var eid = view.data("eventId");
  var event = $.grep(app.events, function(e){ return e.eventId === eid; })[0];

  // check if user's role is editor or above
  var cal = $.grep(app.calendars, function(e){ return e.calendarId === event.calendarId; })[0];
  if (cal.role === "basic") {
    return;
  }

  // update event editor
  turnEventEdit();

  // unformat and populate
  var start = new Date(event.startDateTime);
  var pickerStart = start.toLocaleDateString() + " " + start.toLocaleTimeString().substr(0,5);
  var end = new Date(event.endDateTime);
  var pickerEnd = end.toLocaleDateString() + " " + end.toLocaleTimeString().substr(0,5);

  var form = $("#event_form");
  form.find('input[name="title"]').val(event.title);
  form.find('textarea[name="description"]').val(event.description);
  form.find('input[name="startDate"]').val(pickerStart);
  form.find('input[name="endDate"]').val(pickerEnd);
  form.find('input[name="location"]').val(event.location);
  form.find('input[name="max"]').val(event.max);
  form.find('input[name="eventId"]').val(event.eventId);
  form.find('select[name="calendarId"]').val(event.calendarId);
}

// Adds extra fields into event form
function formatEventForm(formObj) {
  var regex = new RegExp('/', "g");
  formObj["startTime"] = formObj["startDate"].split(" ")[1];
  formObj["startDate"] = formObj["startDate"].split(" ")[0].replace(regex, '-');
  formObj["endTime"] = formObj["endDate"].split(" ")[1];
  formObj["endDate"] = formObj["endDate"].split(" ")[0].replace(regex, '-');
  formObj["timezone"] = jstz.determine().name();
}

// Shows event create button and hide the rest
function turnEventCreate() {
  $("#btn_event_create").show();
  $("#btn_event_save, #btn_event_delete, #btn_event_cancel").hide();
}

// Shows event editing buttons and hides the create
function turnEventEdit() {
  $("#btn_event_create").hide();
  $("#btn_event_save, #btn_event_delete, #btn_event_cancel").show();
}

// Updates description left characters
function updateCountdown() {
    var remaining = 255 - $('#event_form textarea[name="description"]').val().length;
    $('.countdown').text(remaining + ' characters remaining.');
}
