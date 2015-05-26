// DOCUMENT READY
$(function() {
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

  // Bind logout button
  $("#btn_logout").click(function() {
    $.ajax("/api/session", {
      method: "DELETE",
      statusCode: {
        200: function(data) {
          window.location.reload()
        },
        400: function(data) { alert(data.responseJSON.message); }
      }
    })
  })

  // Bind event creation form
  $('#event_form').submit(function(e) {
    e.preventDefault()
    var form = $(this)
    $.ajax(form.attr('action'), {
      method: form.attr('method'),
      data: JSON.stringify(getFormObj(form)),
      statusCode: {
        200: function(data) {
          toastr.success(data.responseJSON.message)
        },
        400: function(data) {
          toastr.error(data.responseJSON.message)
        }
      }
    })
  })

  // Bind calendar creation form
  $('#calendar_create_form').submit(function(e) {
    e.preventDefault()
    var form = $(this)
    $.ajax(form.attr('action'), {
      method: form.attr('method'),
      data: JSON.stringify(getFormObj(form)),
      statusCode: {
        200: function(data) {
          toastr.success(data.message)
        },
        400: function(data) {
          toastr.error(data.message)
        }
      }
    })
  })

  // Sets up request headers for all subsequent ajax calls
  $.ajaxSetup({
    contentType: 'application/json; charset=utf-8',
    dataType: 'json',
    beforeSend: function(xhr) {
      xhr.setRequestHeader("Authorization", getCookie("token"))
    }
  })

  // Request user profile information
  $.ajax('/api/user', {
    method: 'GET',
    statusCode: {
      200: function(data) {
        $('.firstName').text(data.firstName)
      }
    }
  })
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

// Convert input fields into an javascript object
function getFormObj(form) {
  var formObj = {};
  var inputs = form.serializeArray();
  $.each(inputs, function (i, input) {
    formObj[input.name] = input.value;
  });
  return formObj;
}
