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

    // validate form
    formatEventForm(formObj);
    // ajax put
    $.ajax(form.attr("action") +"/"+formObj.eventId, {
      method: "PUT",
      data: JSON.stringify(formObj),
      success: function(data) {
        toastr.success("Saved chanages to " + formObj["title"]);
        refreshEvents();
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
        refreshEvents();
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
	  method: 'GET',
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
  app.events.sort(function(a,b) {
    // Something like
    var aDate = new Date(a.startDateTime);
    var bDate = new Date(b.startDateTime);
    return aDate >= bDate;
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
  '<div class="event">'+
    '<div class="header progress-bar-info">'+
      '<div class="dropdown">'+
        '<a class="label label-warning dropdown-toggle count" id="dropdownMenu{{eventId}}" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">{{remaining}}</a>'+
        '<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu{{eventId}}">'+
          '<li role="presentation" class="dropdown-header">List of Attendees</li>'+
        '</ul>'+
      '</div>'+
      '<div class="dropdown pull-right">'+
        '<button class="btn btn-info more dropdown-toggle" data-toggle="dropdown" aria-expanded="false"><span class="caret"></span></button>'+
        '<ul class="dropdown-menu" role="menu">'+
          '<li class="bg-info"><a href="data:text/calendar;charset=utf8,{{ics}}">Get iCal Event</a></li>'+
          '<li class="bg-warning"><a href="#" onclick="saveEvent(this)">Save Event</a></li>'+
          '<li class="divider"></li>'+
          '<li class="bg-danger"><a href="#" onclick="deleteEventById({{eventId}})">Delete Event</a></li>'+
        '</ul>'+
      '</div>'+
      '<div class="time">'+
        '<dd>{{startTime}}</dd>'+
        '<dd>{{duration}}</dd>'+
      '</div>'+
    '</div>'+
    '<div class="title" onclick="editEvent(this)">{{title}}</div>'+
    '<div class="event-extras">'+
      '<div class="desc" onclick="editEvent(this)">{{description}}</div>'+
      '<div class="requirements"></div>'+
    '</div>'+
    '<div class="location">'+
      '<span class="glyphicon glyphicon-map-marker"></span> {{location}}'+
    '</div>'+
    '<div class="join">'+
      '<a class="badge" onclick="joinEvent(this)">Join</a>'+
    '</div>'+
  '</div>';
  
  // Extract event data
  var start = new Date(event.startDateTime);
  var end = new Date(event.endDateTime);
  var today = new Date();
  
  var timeDiff = Math.abs(end - start); // in milliseconds
  var diffMinutes = Math.ceil(timeDiff / (1000 * 60));
  var hours = Math.floor(diffMinutes/60);
  var minutes = diffMinutes % 60;
  var duration = ((hours > 0) ? (hours + "h") : "") + ((minutes > 0) ? (minutes + "m") : "");
  
  var readableTime = start.toLocaleTimeString().substring(0, 5);
  
  // Extract requirements from description
  var lines = event.description.split("\n");
  var requirements = [];
  var description = [];
  lines.forEach(function(l) {
    if (startsWith(l, "R-")) {
      requirements.push(l.slice(2));
    } else {
      description.push(l);
    }
  });
  // TODO: Maybe remove trailing new lines from the description?
  // as in ["", "desc"] or ["desc", ""]
  description = description.join("\n");
  
  // Find the correct column to place it
  $("#t_calendar_body").children().each(function(k, elem) {
    if ($(elem).data("date") === start.toLocaleDateString()) {
      // append event div
      temp = temp
        .replace('{{eventId}}', event.eventId)
        .replace('{{eventId}}', event.eventId)
        .replace('{{eventId}}', event.eventId)
        .replace('{{startTime}}', readableTime)
        .replace('{{duration}}', duration)
        .replace('{{title}}', event.title)
        .replace('{{description}}', description)
        .replace('{{location}}', event.location)
        .replace('{{ics}}', createICSFile(event));
      
      if (event.max == -1) {
        temp = temp.replace('{{remaining}}', "&infin;");
      } else {
        temp = temp.replace('{{remaining}}', event.currentCount + "/" + event.max);
      }
      
      // stop propagating event to elements below this view
      var view = $(temp).data("eventId", event.eventId).click(function(e) {
        e.stopPropagation();
      });

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

      // show list of volunteers if admin clicks on label
      view.find(".count").dropdown().click(function() {
        var attendeesList = $(this).next();
        var tmpl =
          '<li data-user-id="{{userId}}" role="presentation">'+
            '<a role="menuitem" tabindex="-1" href="#">'+
              '<span>{{firstName}}</span>'+
              '<span class="glyphicon glyphicon-minus-sign pull-right btn-remove" onclick="removeAttendee(this)"></span>'+
            '</a>'+
          '</li>';
        // By the time we get here it is considered open
        if (!$(this).parent().hasClass("open")) {
          return;
        }
        $.ajax("/api/event/" + event.eventId, {
          method: "GET",
          success: function(data) {
            attendeesList.children().remove(":not(.dropdown-header)");
            if (data.attendees.length > 0) {
              // add attendees
              $.each(data.attendees, function(k, attendee) {
                var elem = $(tmpl
                    .replace("{{userId}}", attendee.userId)
                    .replace("{{firstName}}", attendee.firstName));
                // disable removing admin himself from event (use unjoin instead)
                if (attendee.userId === app.user.userId) {
                  elem.find(".btn-remove").hide();
                }
                elem.appendTo(attendeesList);
              });
            }
          },
          error: function(data) {
            attendeesList.children().remove(":not(.dropdown-header)");
            $(tmpl).appendTo(attendeesList).find("a").text("- Private -");
          }
        });
      });

      // Append the view to the actual td
      $(elem).append(view);
      if (event.hasJoined) {
        // turn header bar green
        view.find(".header").removeClass("progress-bar-info").addClass("progress-bar-success");
        view.find(".more").removeClass("btn-info").addClass("btn-success");
        // update joined badge
        view.find(".badge").addClass("progress-bar-danger").text("Unjoin");
        // update requirements checkbox
        view.find('.requirements input[type="checkbox"]').each(function(i) {
          this.checked = true;
          this.disabled = true;
        });
      }
      
      // Hide if in the past or there are any places
      if (today >= end || event.max - event.currentCount == 0) {
        view.find(".badge").hide();
        // turn header bar of unjoined events grey
        if (!event.hasJoined) {
          view.find(".header").removeClass("progress-bar-info").addClass("label-default");
          view.find(".more").removeClass("btn-info");
        }
      }
      
      // enable more actions button
      view.find(".more").dropdown();
      
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
    $.ajax("/api/subscription/event/" + eid, {
      method: "DELETE",
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
        // update requirements checkbox
        view.find('.requirements input[type="checkbox"]').each(function(i) {
          this.checked = false;
          this.disabled = false;
        });
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
        // update requirements checkbox
        view.find('.requirements input[type="checkbox"]').each(function(i) {
          this.disabled = true;
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
  var event = $.grep(app.events, function(e){ return e.eventId === eid; })[0];

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
function updateAttendeeCount(view, delta) {
  var eid = view.data("eventId");
  var event = $.grep(app.events, function(e){return e.eventId === eid})[0];
  if (event) {
    event.currentCount += delta;
    view.find(".count").text(event.currentCount + "/" + event.max);
  }
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
  var event = $.grep(app.events, function(e){return e.eventId === eventId;})[0];

  // User confirmation
  if(!confirm("Are you sure you want to delete "+event.title+"?")) {
    return
  }

  // ajax delete
  $.ajax("/api/event/"+event.eventId, {
    method: "DELETE",
    success: function(data) {
      toastr.success("Deleted event " + event.title);
      refreshEvents();
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
