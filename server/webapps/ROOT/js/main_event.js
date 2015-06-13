// Document Ready
$(function() {
  // Bind datetime picker
  // http://xdsoft.net/jqplugins/datetimepicker/
  $(".datetimepicker").each(function(k, elem) {
    $(elem).datetimepicker({
      format: "Y-m-d H:i"
    });
  });
  
  // Bind the pick start date input
  $("#pickStartDate").datetimepicker({
    format: "Y-m-d"
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
    var controller = getEventById(formObj.eventId);

    // validate form
    formatEventForm(formObj);
    // ajax put
    $.ajax(form.attr("action") +"/"+formObj.eventId, {
      method: "PUT",
      data: JSON.stringify(formObj),
      success: function(data) {
        toastr.success("Saved chanages to " + formObj["title"]);
        controller.update(formObj);
        resetEventForm();
      },
      error: function(data) { $("#event_create_errors").text(data.responseJSON.message); }
    })
  });

  $("#btn_event_clear, #btn_event_cancel").click(resetEventForm);

  // delete event
  $("#btn_event_delete").click(function() {
    // Retrieve form data
    var form = $("#event_form");
    var formObj = getFormObj(form);
    if (!formObj.eventId) {
      toastr.error("Failed to read event id. Please select the event again or refresh the app.");
      return;
    }

    deleteEventById(formObj.eventId);
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
        // TODO: fix duplicate dynamic update from backend, rely on chat for now
        // var event = new Event(data);
        // app.events.push(event);
        // event.render();
        resetEventForm();
      },
      error: function(data) { $("#event_create_errors").text(data.responseJSON.message); }
    });
  });

  // Refresh description count
  $('#event_form textarea[name="description"]').change(updateCountdown);
  $('#event_form textarea[name="description"]').keyup(updateCountdown);
  updateCountdown();

  $.ajax("/api/save/event", {
    method: "GET",
    success: function(data) {
      var saved = $("#collapseThree .list-group");
      var tmpl = 
          '<a href="#" class="list-group-item" data-event-id="{{eventId}}">'+
            '<span>{{title}}</span>'+
            '<span class="glyphicon glyphicon-minus-sign pull-right btn-remove" onclick=event.stopPropagation();removeSavedEvent(this)></span>'+
          '</a>';
      $.each(data.savedEvents, function(k, event) {
        var elem = tmpl
            .replace("{{title}}", event.title)
            .replace("{{eventId}}", event.eventId);
        $(elem).click(function() {
          // update event creation form
          updateEventForm(event);
        }).appendTo(saved);
      });
    },
    error: function(data) {
      toastr.error("Failed to retrieve past events: " + data.responseJSON.message);
    }
  });
}); // End of document ready

// Update Events
function refreshEvents() {
  // Retrieve and render calendar events
  app.events = app.events.filter(function(e) {
    e.view.remove();
    return false;
  });
  updateCalendarDates(app.current_start_date);
  var active_calendars = getActiveCalendarIds();
  // Get event data for the active calendars then render
  $.each(active_calendars, function(index, cid) {
    getCalendarEventsByCid(cid);
  });
}

function getCalendarEventsByCid(cid) {
  $.ajax("/api/calendar/"+cid, {
    method: 'GET',
    data: {startDate: app.current_start_date.toJSON().split('T')[0] + " 00:00:00"},
    success: function(data) {
      app.events.push.apply(app.events, data.events.map(function(e) {
        var event = new Event(e);
        event.render();
        return event;
      }));
    },
    error: function(data) {
      toastr.error("Failed to get events for " + id);
    }
  });
}

// Join an event
function joinEvent(elem) {
  var view = $(elem).closest(".event");
  var eid = view.data("eventId");
  var controller = getEventById(eid);
  var event = controller.model;
  
  // determine wether to join or unjoin
  if (event.hasJoined) {
    // unjoin an event
    $.ajax("/api/subscription/event/" + eid, {
      method: "DELETE",
      success: function(data) {
        controller.update({
          currentCount: event.currentCount - 1,
          hasJoined: false
        });
        toastr.warning("Unjoined event " + event.title);
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
    
    // Satisfy all requirements if there are any
    var filledReqs = true;
    view.find('.requirements input[type="checkbox"]').each(function(i) {
      filledReqs = filledReqs && this.checked;
    });
    
    if (!filledReqs) {
      toastr.error("You haven't checked all requirements for " + event.title);
      return;
    }
    
    // Everything is fine, join the party
    $.ajax("/api/subscription/event/" + eid, {
      method: "POST",
      success: function(data) {
        toastr.success("Joined event " + event.title);
        controller.update({
          currentCount: event.currentCount + 1,
          hasJoined: true
        });
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
  var controller = getEventById(eid);
  var event = controller.model;

  // check if user's role is editor or above
  var cal = $.grep(app.calendars, function(e){ return e.calendarId === event.calendarId; })[0];
  if (cal.role === "basic") {
    return;
  }

  // update event editor
  turnEventEdit();

  // unformat and populate
  updateEventForm(event);
}

function updateEventForm(event) {
  // unformat and populate
  var start = new Date(event.startDateTime);
  var end = new Date(event.endDateTime);

  var form = $("#event_form");
  form.find('input[name="title"]').val(event.title);
  form.find('textarea[name="description"]').val(event.description);
  form.find('input[name="startDate"]').datetimepicker({value: start});
  form.find('input[name="endDate"]').datetimepicker({value: end});
  form.find('input[name="location"]').val(event.location);
  form.find('input[name="max"]').val(event.max);
  form.find('input[name="eventId"]').val(event.eventId);
  form.find('select[name="calendarId"]').val(event.calendarId);

  // update number of characters remaining in description field
  updateCountdown();
}

// Adds extra fields into event form
function formatEventForm(formObj) {
  var start = new Date(formObj["startDate"]);
  var end = new Date(formObj["endDate"]);
  formObj["startDateTime"] = start.toJSON();
  formObj["endDateTime"] = end.toJSON();

  var regex = new RegExp('/', "g");
  formObj["startTime"] = formObj["startDate"].split(" ")[1];
  formObj["startDate"] = formObj["startDate"].split(" ")[0].replace(regex, '-');
  formObj["endTime"] = formObj["endDate"].split(" ")[1];
  formObj["endDate"] = formObj["endDate"].split(" ")[0].replace(regex, '-');
  formObj["timezone"] = jstz.determine().name();
}

// Shows event create button and hide the rest
function turnEventCreate() {
  $("#btn_event_create, #btn_event_clear").show();
  $("#btn_event_save, #btn_event_delete, #btn_event_cancel").hide();
}

// Shows event editing buttons and hides the create
function turnEventEdit() {
  $("#btn_event_create, #btn_event_clear").hide();
  $("#btn_event_save, #btn_event_delete, #btn_event_cancel").show();
}

// Updates description left characters
function updateCountdown() {
    var remaining = 255 - $('#event_form textarea[name="description"]').val().length;
    $('.countdown').text(remaining + ' characters remaining.');
}

// Resets the event form to create
function resetEventForm() {
  $("#event_form").trigger('reset');
  turnEventCreate();
  updateCountdown();
  $("#event_create_errors").empty();
}

// increment or decrement attendee count by delta
function updateAttendeeCount(eventId, delta) {
  // update count badge if event is rendered in calendar
  $(".event").each(function(k, elem) {
    var view = $(elem);
    if (view.data("eventId") == eventId) {
      var event = $.grep(app.events, function(e){return e.eventId == eventId})[0];
      if (event) {
        event.currentCount += delta;
        view.find(".count").text(event.currentCount + "/" + event.max);
      }
    }
  });
}

// Removes an attendee from an event (admin feature)
function removeAttendee(elem) {
  var user = $(elem).closest("li");
  var event = $(elem).closest(".event");

  // unsubscribe user from event
  $.ajax("/api/subscription/event/" + event.data("eventId"), {
    method: "DELETE",
    data: JSON.stringify({userId: user.data("userId")}),
    success: function(data) {
      user.remove();
      //TODO: count will be updated upon receiving unjoin notification
      //updateAttendeeCount(event, -1);
    },
    error: function(data) {
      toastr.error(data.responseJSON.message);
    }
  });
}

function deleteEventById(eventId) {
  var controller = getEventById(eventId);
  var event = controller.model;

  // User confirmation
  if(!confirm("Are you sure you want to delete "+event.title+"?")) {
    return
  }

  // ajax delete
  $.ajax("/api/event/"+event.eventId, {
    method: "DELETE",
    success: function(data) {
      toastr.success("Deleted event " + event.title);
      var index = app.events.indexOf(controller);
      app.events.splice(index, 1);
      controller.view.remove();
      resetEventForm();
    },
    error: function(data) { $("#event_create_errors").text(data.responseJSON.message); }
  });
}

function saveEvent(elem) {
  var view = $(elem).closest(".event");
  var savedEvents = $("#collapseThree .list-group");
  var eid = view.data("eventId");
  var title = view.find(".title").html();
  $.ajax({
    method: "POST",
    url: "/api/save/event/" + eid,
    success: function() {
      /* Traverse all child DOM elements and remove the one (if exists) that
         has the same event ID as the ID of the event to be added. */
      $(savedEvents).children().each(function() {
        var curEid = $(this).attr("data-event-id");
        if (curEid == eid) {
          $(this).remove();
        }
      });
      var tmpl = 
          '<a href="#" class="list-group-item" data-event-id="{{eventId}}">'+
            '<span>{{title}}</span>'+
            '<span class="glyphicon glyphicon-minus-sign pull-right btn-remove" onclick=event.stopPropagation();removeSavedEvent(this)></span>'+
          '</a>';
      var savedEvent = tmpl
            .replace("{{title}}", title)
            .replace("{{eventId}}", eid);
      savedEvents.prepend(savedEvent);
      toastr.success("Saved event "+title);
    }
  });
}

// Removes an event from list of saved event templates
function removeSavedEvent(elem) {
  var event = $(elem).closest("a");
  address = "api/save/event/";
  $.ajax({
    method: "DELETE",
    url: address.concat(event.data("eventId")),
    success: function(data) {
      event.remove();
    }
  });
}

// creates a downloadable ics file
function createICSFile (event) {
  var start = jsonToICS(event.startDateTime);
  var end = jsonToICS(event.endDateTime);
  var tmpl = [
    "BEGIN:VCALENDAR",
    "VERSION:2.0",
    "PRODID:-//Our Company//NONSGML v1.0//EN",
    "BEGIN:VEVENT",
      "UID:me@google.com",
      "DTSTAMP:20120315T170000Z",
      "ATTENDEE;CN=My Self ;RSVP=TRUE:MAILTO:me@gmail.com",
      "ORGANIZER;CN=Me:MAILTO:me@gmail.com",
      "DTSTART:"+start,
      "DTEND:"+end,
      "LOCATION:"+event.location,
      "SUMMARY:"+event.title,
      "DESCRIPTION:"+event.description,
      "BEGIN:VALARM",
        "TRIGGER:-PT30M",
        "ACTION:DISPLAY",
        "DESCRIPTION:Reminder",
      "END:VALARM",
    "END:VEVENT",
    "END:VCALENDAR"
  ];
  return escape(tmpl.join("\n"));
}

// convert json date string to iCalendar format
function jsonToICS(date) {
  return date.split("-").join("").split(":").join("");
}

function getEventById(eid) {
  return $.grep(app.events, function(e){ return e.model.eventId == eid; })[0];
}
