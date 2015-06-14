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
    // TODO: check if this train reck is the only way to do this
    var sat_index = $('#t_calendar_heading th:contains("Sat")').index()+1;
    var selector = "#t_calendar th:nth-of-type("+sat_index+"), #t_calendar td:nth-of-type("+sat_index+"), #t_calendar th:nth-of-type("+(sat_index+1)+"), #t_calendar td:nth-of-type("+(sat_index+1)+")";
    $(this).parent().toggleClass("active");
    $(selector).toggle();
  });

  // Bind sidebar collapse
  $("#b_hide_left").click(function() {
    $(this).parent().hasClass("active") ? hideLeftBar() : showLeftBar();
    //rebuildCalendar();
  });
  
  // Bind right side bar
  $("#b_hide_right").click(function() {
    $(this).parent().hasClass("active") ? hideRightBar() : showRightBar();
    //rebuildCalendar();
  });

  // mobile actions
  $(window).on("swipeleft", function(e) {
    $(".app").hasClass("showleft") ? $(".app").removeClass("showleft") : $(".app").addClass("showright");
  });

  $(window).on("swiperight", function(e) {
    $(".app").hasClass("showright") ? $(".app").removeClass("showright") : $(".app").addClass("showleft");
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

  // Bind previous and next day button
  $("#prev_day").click(function() {
    // advance date by 1
    var days = $("#t_calendar_heading").children(":visible").length;
    app.current_start_date.setDate(app.current_start_date.getDate() - days);
    refreshEvents();
  });

  $("#next_day").click(function() {
    // shift weekday columns right by one
    var days = $("#t_calendar_heading").children(":visible").length;
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

  // TODO: handle device orientation change
  //$(window).resize(rebuildCalendar);
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
        // nope: 'geo-polyfill.js',
        complete: function() {
          // initialise first for easier debugging
          app.adapter = new NotificationAdapter();
          $.chat({
            // your user information
            userId: app.user.userId,
            // id of the room. The friends list is based on the room Id
            roomId: 1,
            // text displayed when the other user is typing
            typingText: ' is typing...',
            // text displayed when there's no other users in the room
            //emptyRoomText: "There's no one around here. You can still open a session in another browser and chat with yourself :)",
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
  $(".app").addClass("showright");
  // $("#d_right_sidebar").addClass("active");
  $("#b_hide_right").parent().addClass("active");
}

function showLeftBar() {
  $(".app").addClass("showleft");
  // $("#d_left_sidebar").addClass("active");
  $("#b_hide_left").parent().addClass("active");
}

// Rebuild calendar layout for mobile responsiveness
function rebuildCalendar() {
  // get available space
  var width = (window.innerWidth > 0) ? window.innerWidth : screen.width;
  if (width < 768) {
    // single column fluid layout
    if ($("#t_calendar_body").children().length !== 1) {
      // clear calendar heading and body
      $("#t_calendar_heading").empty().append("<th/>");
      $("#t_calendar_body").empty().append("<td/>");
      app.current_start_date = new Date();
      refreshEvents();
    }
  } else {
    var left = $("#b_hide_left").parent().hasClass("active") ? $("#d_left_sidebar").outerWidth() : 0;
    var right = $("#b_hide_right").parent().hasClass("active") ? $("#d_right_sidebar").outerWidth() : 0;
    width = width - left - right;
    var days = width / 160 >> 0;
    var current_days = $("#t_calendar_body").children().length;

    if (days !== current_days) {
      var dayOfWeek = app.current_start_date.getDay();

      // clear calendar heading and body
      $("#t_calendar_heading").empty();
      $("#t_calendar_body").empty();

      $(".container").animate({
        "left": left,
        duration: 0.2
      }).css("width", width - 30);

      for (var i = 0; i < days; i++) {
        $("#t_calendar_heading").append("<th/>");
        $("#t_calendar_body").append("<td/>");
      }

      // update navigation button
      refreshEvents();
    }
  }
}
