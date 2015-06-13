var Event = (function() {
  // class variables
  Event.template =
      '<div class="event">'+
        '<div class="header progress-bar-info">'+
          '<div class="dropdown">'+
            '<a class="label label-warning dropdown-toggle count" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"></a>'+
            '<ul class="dropdown-menu" role="menu">'+
              '<li role="presentation" class="dropdown-header">List of Attendees</li>'+
            '</ul>'+
          '</div>'+
          '<div class="dropdown pull-right">'+
            '<button class="btn dropdown-toggle more btn-info" data-toggle="dropdown" aria-expanded="false"><span class="caret"></span></button>'+
            '<ul class="dropdown-menu" role="menu">'+
              '<li><a href="#" onclick="deleteEventById({{eventId}})">Delete Event</a></li>'+
              '<li class="divider"></li>'+
              '<li><a href="data:text/calendar;charset=utf8,{{ics}}">Add to Calendar</a></li>'+
              '<li><a href="#" onclick="saveEvent(this)">Add to Saved Events</a></li>'+
            '</ul>'+
          '</div>'+
          '<div class="time">'+
            '<dd></dd>'+
            '<dd></dd>'+
          '</div>'+
        '</div>'+
        '<div class="title" onclick="editEvent(this)"></div>'+
        '<div class="event-extras">'+
          '<div class="desc" onclick="editEvent(this)"></div>'+
          '<div class="requirements"></div>'+
        '</div>'+
        '<div class="location">'+
          '<span class="glyphicon glyphicon-map-marker"></span>'+
          '<span></span>'+
        '</div>'+
        '<div class="join">'+
          '<a class="badge" onclick="joinEvent(this)">Join</a>'+
        '</div>'+
      '</div>';

  function Event(model) {
    // save model as private field
    this.model = {eventId: model.eventId};

    // construct the view for rendering
    this.view = $(Event.template
      .replace('{{eventId}}', model.eventId)
      .replace('{{ics}}', createICSFile(model)));

    // stop propagating event to elements below this view
    this.view.data("eventId", model.eventId).click(function(e) {
      e.stopPropagation();
    });

    // show list of volunteers if admin clicks on label
    this.view.find(".count").dropdown().click(function() {
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
      $.ajax("/api/event/" + model.eventId, {
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
    
    // enable more actions button
    this.view.find(".more").dropdown();

    this.update(model);
  }

  Event.prototype.render = function() {
    // finds the day on calendar (if visible) and append view
    var start = new Date(this.model.startDateTime);
    if (!$.contains(document, this.view)) {
      $("#t_calendar_body").children(":visible").filter(function(k, elem) {
        return $(elem).data("date") === start.toLocaleDateString();
      }).append(this.view);
    }
  };

  /**
  * Updates the underlying model, view, and remote partially.
  */
  Event.prototype.update = function(model) {
    // build partial update object
    var partial = {'eventId': this.model.eventId};
    for (var key in model) {
      var value = model[key];
      if (!this.model[key] || this.model[key] !== value) {
        // updates the underlying model and view
        this.model[key] = value;
        var handler = this.modelChanged[key];
        // if handler is defined, calls apply to pass in event object as this
        if (handler) {
          handler.apply(this);
        } else {
          // console.log(key);
        }
        partial[key] = value;
      }
    }

    // TODO: send changes to remote
  };

  /**
  * Change the view to reflect new model
  */
  Event.prototype.modelChanged = {
    title: function() {
      this.view.find(".title").text(this.model.title);
    },
    description: function() {
      // Extract requirements from description
      var lines = this.model.description.split("\n");
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
      this.view.find(".desc").text(description);

      // Add in the requirement checkboxes
      var req = this.view.find(".requirements");
      var req_checkbox =
          '<div class="checkbox"> \
            <label> \
              <input type="checkbox"> {{requirement}} \
            </label> \
          </div>';
      requirements.forEach(function(r) {
        $(req_checkbox.replace("{{requirement}}", r)).appendTo(req);
      });
    },
    currentCount: function() {
      // update count badge
      var count = this.view.find(".count");
      if (this.model.max === -1) {
        count.html("&infin;");
      } else {
        count.text(this.model.currentCount + "/" + this.model.max);
        this.isFull = (this.model.max - this.model.currentCount === 0);
      }

      // show / hide join button
      if (!this.isFull && !this.isPast) {
        this.view.find(".join .badge").show();
      } else {
        this.view.find(".join .badge").hide();
      }
    },
    max: function() {
      this.modelChanged.currentCount.apply(this);
    },
    location: function() {
      // hide location if it is not set
      if (this.model.location) {
        this.view.find(".location").show().children().last().text(" " + this.model.location);
      } else {
        this.view.find(".location").hide();
      }
    },
    hasJoined: function() {
      // turn header bar of unjoined events grey
      if (this.model.hasJoined) {
        // turn header bar green
        this.view.find(".header").removeClass("progress-bar-info label-default").addClass("progress-bar-success");
        this.view.find(".more").removeClass("btn-info").addClass("btn-success");
        // update joined badge
        this.view.find(".join .badge").addClass("progress-bar-danger").text("Unjoin");
        // update requirements checkbox
        this.view.find('.requirements input[type="checkbox"]').each(function(k, elem) {
          elem.checked = true;
          elem.disabled = true;
        });
      } else {
        // update header of present events
        if (!this.isPast) {
          this.view.find(".header").addClass("progress-bar-info").removeClass("progress-bar-success");
          this.view.find(".more").addClass("btn-info").removeClass("btn-success");
        }
        // update badge
        this.view.find(".join .badge").removeClass("progress-bar-danger").text("Join");
        // update requirements checkbox
        this.view.find('.requirements input[type="checkbox"]').each(function(k, elem) {
          elem.checked = false;
          elem.disabled = false;
        });
      }
    },
    startDateTime: function() {
      var start = new Date(this.model.startDateTime);
      var readableTime = start.toLocaleTimeString().substring(0, 5);
      this.view.find(".time").children().first().text(readableTime);
    },
    endDateTime: function() {
      // Extract event data
      var start = new Date(this.model.startDateTime);
      var end = new Date(this.model.endDateTime);
      var today = new Date();
      
      var timeDiff = Math.abs(end - start); // in milliseconds
      var diffMinutes = Math.ceil(timeDiff / (1000 * 60));
      var hours = Math.floor(diffMinutes/60);
      var minutes = diffMinutes % 60;
      var duration = ((hours > 0) ? (hours + "h") : "") + ((minutes > 0) ? (minutes + "m") : "");

      this.view.find(".time").children().last().text(duration);
      this.isPast = (today > end);

      if (this.isPast) {
        this.view.find(".header").removeClass("progress-bar-info").addClass("label-default");
        this.view.find(".more").removeClass("btn-info");
        this.view.find(".join .badge").addClass("hidden");
      } else {
        if (!this.model.hasJoined) {
          this.view.find(".header").addClass("progress-bar-info").removeClass("label-default");
          this.view.find(".more").addClass("btn-info");
        }
        if (!this.isFull) {
          this.view.find(".join .badge").show();
        }
      }
    }
  };

  return Event;
})();