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
  
  // Bind user profile buttons
  $("#b_update_profile").click(function() {
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
    app.current_start_date.setDate(app.current_start_date.getDate() - 7);
    refreshEvents();
  });

  $("#next_day").click(function() {
    // shift weekday columns right by one
    app.current_start_date.setDate(app.current_start_date.getDate() + 7);
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
  
  // Initialize chat service
  Chat.init();
  
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
