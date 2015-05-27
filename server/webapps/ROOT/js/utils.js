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
