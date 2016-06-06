var app = {joined:{}};

// DOCUMENT READY
$(function() {
  // Sets up request headers for all subsequent ajax calls
  $.ajaxSetup({
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    beforeSend: function(xhr) {
      xhr.setRequestHeader("Authorization", getCookie("token"));
    }
  });

  $("#pickStartDate").datetimepicker({
    timepicker: false,
    onSelectDate: function(dp,$input){
      app.current_start_date = dp;
      refreshEvents();
    }
  });
  
  // Bind refresh button
  $("#b_refresh").click(refreshCalendars);
  
  // Bind weekend collapse
  $("#b_hide_weekend").click(function(){
    $(this).parent().toggleClass("active");
    app.hide_weekend = !$(this).parent().hasClass("active");
    refreshEvents();
  });

  // Bind sidebar collapse
  $("#b_hide_left").click(function() {
    $(this).parent().hasClass("active") ? hideLeftBar() : showLeftBar();
  });
  
  // Bind right side bar
  $("#b_hide_right").click(function() {
    $(this).parent().hasClass("active") ? hideRightBar() : showRightBar();
  });

  // mobile actions
  $(window).on("swipeleft", function(e) {
    var chat = $(e.target).closest(".chat-window")[0];
    if (chat) {
      var chats = $(".chat-window");
      var width = 235;
      // check if there's space on the left
      var position = parseInt(chats.first().css("right").slice(0, -2));
      if (position + width*2 < $(window).width()) {
        // move all chats left by 1 box
        chats.each(function(k, chat) {
          var pos = parseInt($(chat).css("right").slice(0, -2));
          $(chat).css({
            right: pos + width + "px"
          })
        });
      }
    } else {
      var app = $(".app");
      if (app.hasClass("showleft")) {
        app.removeClass("showleft");
        $("#b_hide_left").parent().removeClass("active");
      } else {
        app.addClass("showright");
        $("#b_hide_right").parent().addClass("active");
      }
    }
  });

  $(window).on("swiperight", function(e) {
    var chat = $(e.target).closest(".chat-window")[0];
    if (chat) {
      var chats = $(".chat-window");
      var width = 235;
      // check if there's space on the right
      var position = parseInt(chats.last().css("right").slice(0, -2));
      if (position > 10) {
        // move all chats right by 1 box
        chats.each(function(k, chat) {
          var pos = $(chat).css("right").slice(0, -2);
          $(chat).css({
            right: pos - width + "px"
          })
        });
      }
    } else {
      var app = $(".app");
      if (app.hasClass("showright")) {
        app.removeClass("showright");
        $("#b_hide_right").parent().removeClass("active");
      } else if ($("#b_hide_left").is(":visible")
          || !$("#b_hide_right").is(":visible")) {
        app.addClass("showleft");
        $("#b_hide_left").parent().addClass("active");
      }
    }
  });

  // Bind logout button
  $("#b_logout").click(function() {
    $.ajax("/api/session", {
      method: "DELETE",
      success: function(data) { window.location.reload(); },
      error: function(data) { alert(data.responseJSON.message); }
    });
  });
  
  // Bind user profile buttons
  $("#b_update_profile").click(function(e) {
    if (!$(this).closest(".panel-heading").hasClass("collapsed")) {
      e.stopPropagation();
    }
    $("#d_user_profile").toggle();
    $("#profile_form").toggle();
  });
  $("#b_cancel_profile").click(function() {
    $("#profile_form").toggle();
    $("#d_user_profile").toggle();
  });
  
  // Bind user profile update form
  $("#profile_form").submit(function(e) {
    e.preventDefault();
    var form = $(this);
    
    // Validate
    if (validateUpdate(form)) {
      return;
    }

    submitAjaxForm(form, function(data) { toastr.success(data.message); $("#b_cancel_profile").click(); refreshUser(); }, $("#profile_errors"));
  });
  
  // Bind the account deletion button
  $("#b_delete_account").click(function() {
    for(var i = 0; app.calendars.length; i++) {
      if (app.calendars[i].role == "admin") {
        alert("You are still an admin of " + app.calendars[i].name + ".\nPlease ask a fellow admin to demote you first, or delete the calendar.");
        return;
      }
    }
    if(confirm("Are you sure you want to delete your account?")) {
      $.ajax("/api/user", {
        method: "DELETE",
        success: function(data) { window.location.reload(); },
        error: function(data) { alert(data.responseJSON.message); }
      });
    }
  });

  // Bind previous and next day button
  $("#prev_day").click(function() {
    // advance date by 1
    var days = $("#t_calendar_heading").children(":visible").length;
    if (app.hide_weekend) {
      var startDay = app.current_start_date.getDay();
      if (startDay - days <= 0) {
        days += 2;
      }
    }
    app.current_start_date.setDate(app.current_start_date.getDate() - days);
    refreshEvents();
  });

  $("#next_day").click(function() {
    // shift weekday columns right by one
    var days = $("#t_calendar_heading").children(":visible").length;
    if (app.hide_weekend) {
      var startDay = app.current_start_date.getDay();
      if (startDay + days >= 6) {
        days += 2;
      }
    }
    app.current_start_date.setDate(app.current_start_date.getDate() + days);
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
  
  // Activate time
  // http://stackoverflow.com/questions/18229022/how-to-show-current-time-in-javascript-in-the-format-hhmmss
  (function () {
    function checkTime(i) {
      return (i < 10) ? "0" + i : i;
    }

    function startTime() {
      var today = new Date(),
      h = checkTime(today.getHours()),
      m = checkTime(today.getMinutes()),
      s = checkTime(today.getSeconds());
      document.getElementById('p_time').innerHTML = h + ":" + m + ":" + s;
      t = setTimeout(function () {
        startTime()
      }, 500);
    }
    startTime();
  })();
}); // End of document ready

// Update user profile information on view
function refreshUser() {
  $.get("/api/user",
    function(data) {
      app.user = data;
      $("[data-bind='email']").text(data.email).val(data.email);
      $("[data-bind='firstName']").text(data.firstName).val(data.firstName);
      $("[data-bind='lastName']").text(data.lastName).val(data.lastName);
      $("[data-bind='lastSeen']").text(data.lastSeen).val(data.lastSeen);
      
      // Chat initialize
      Modernizr.load({
        test: Modernizr.websockets,
        // TODO: load web socket polyfill if not supported
        // nope: 'geo-polyfill.js',
        complete: function() {
          // initialise first for easier debugging
          app.adapter = new NotificationAdapter();
          $.chat({
            // your user information
            userId: app.user.userId,
            // id of the room. The friends list is based on the room Id
            roomId: NotificationServerAdapter.DEFAULT_ROOM_ID,
            // text displayed when the other user is typing
            typingText: ' is typing...',
            // text displayed when there's no other users in the room
            // emptyRoomText: "There's no one around here. You can still open a session in another browser and chat with yourself :)",
            // path to chatjs files
            chatJsContentPath: '/',
            // the adapter you are using
            adapter: app.adapter
          });
        }
      });
  });
}

// Hide sidebar by moving it off screen
function hideRightBar() {
  $(".app").removeClass("showright");
  // $("#d_right_sidebar").removeClass("active");
  $("#b_hide_right").parent().removeClass("active");
}

function hideLeftBar() {
  $(".app").removeClass("showleft");
  // $("#d_left_sidebar").removeClass("active");
  $("#b_hide_left").parent().removeClass("active");
}

// Show sidebar by moving it into screen
function showRightBar() {
  if (window.innerWidth < 1200) {
    hideLeftBar();
  }
  $(".app").addClass("showright");
  // $("#d_right_sidebar").addClass("active");
  $("#b_hide_right").parent().addClass("active");
}

function showLeftBar() {
  if (window.innerWidth < 1200) {
    hideRightBar();
  }
  $(".app").addClass("showleft");
  // $("#d_left_sidebar").addClass("active");
  $("#b_hide_left").parent().addClass("active");
}
