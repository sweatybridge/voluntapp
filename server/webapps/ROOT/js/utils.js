// Convert input fields into an javascript object
function getFormObj(form) {
    var formObj = {};
    var inputs = form.serializeArray();
    $.each(inputs, function (i, input) {
        formObj[input.name] = input.value;
    });
    return formObj;
}

// Submit ajax form with different error field
function submitAjaxForm(form, successCallback, errorDiv) {
  $.ajax(form.attr("action"), {
    data: JSON.stringify(getFormObj(form)),
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    method: form.attr("method"),
    success: successCallback,
    error: function(data) { errorDiv.text(data.responseJSON.message); }
  });
}

// Temporary function to get auth token from cookie
function getCookie(name) {
  var value = "; " + document.cookie;
  var parts = value.split("; " + name + "=");
  if (parts.length == 2) return parts.pop().split(";").shift();
}

// Format date string to May 15
function formatDate(date) {
  var str = date.toDateString();
  return str.substring(str.indexOf(' ') + 1, str.lastIndexOf(' '));
}

// Format day of week, TODO: put this into date prototype
function getWeekDay(date) {
  var str = date.toDateString();
  return str.substring(0, str.indexOf(' '));
}

// Get yesterday as date object
function yesterday() {
  var today = new Date();
  today.setDate(today.getDate() - 1);
  return today;
}
