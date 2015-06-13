// Convert input fields into an javascript object
function getFormObj(form) {
    var formObj = {};
    var inputs = form.serializeArray();
    $.each(inputs, function (i, input) {
        if (input.name === "max" || input.name === "calendarId" || input.name === "eventId") {
          formObj[input.name] = parseInt(input.value);
        } else {
          formObj[input.name] = input.value;
        }
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

// Returns the clients timezone offset
function getTimezoneOffset() {
  // Note it is in minutes and UTC - local, so -60 is actually +1
  // so we divide by -60 to get the human readable offset
  return new Date().getTimezoneOffset()/-60;
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

// http://stackoverflow.com/questions/4156434/javascript-get-the-first-day-of-the-week-from-current-date
function getMonday() {
  d = new Date();
  var day = d.getDay(),
      diff = d.getDate() - day + (day == 0 ? -6:1); // adjust when day is sunday
  return new Date(d.setDate(diff));
}

// Get yesterday as date object
function yesterday() {
  var today = new Date();
  today.setDate(today.getDate() - 1);
  return today;
}

// Validation of update form
function validateUpdate(form) {
  var form = form[0];
  var pass_val = form["password"].value;
  
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

  // Check name only contains alphabet
  if (hasError(form["firstName"].value) || hasError(form["lastName"].value)) {
    $("#profile_errors").text("Name must only contain alphabetic characters.");
    return true;
  }

  return false;
}

// TODO: support more languages
function hasError(name) {
  return (name.search(/[^A-Za-z\s]/) != -1);
}

// Check if strA startsWith strB
function startsWith(strA, strB) {
  return strA.slice(0, strB.length) == strB;
}

// Check if strA endsWith strB
function endsWith(strA, strB) {
  return strA.slice(-strB.length) == strB;
}
