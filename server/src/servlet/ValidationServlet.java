package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.ValidationRequest;
import resp.ErrorResponse;
import resp.Response;
import resp.SuccessResponse;
import resp.UserResponse;
import utils.EmailUtils;
import utils.PasswordUtils;

import com.google.gson.Gson;
import db.CodeGenerator;
import db.DBInterface;
import exception.InconsistentDataException;
import exception.PasswordHashFailureException;
import exception.UserNotFoundException;

public class ValidationServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private final DBInterface db;
  private final Gson gson;

  public static final int VALIDATION_CODE_LENGTH = 20;

  public ValidationServlet(Gson gson, DBInterface db) {
    this.db = db;
    this.gson = gson;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    String email = request.getParameter("email");
    String validationCode = request.getParameter("validationCode");

    Boolean valid;
    try {
      valid = db.checkValidation(email, validationCode);
    } catch (SQLException e) {
      e.printStackTrace();
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Validation check failed"));
      return;
    }
    // Check whether the validation code was correct and return relevant
    // information to the client.
    if (!valid) {
      response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
      request
          .setAttribute(
              Response.class.getSimpleName(),
              new ErrorResponse(
                  "You have entered an invalid validation key, please check your email"));
      return;
    } else {
      request.setAttribute(Response.class.getSimpleName(), new SuccessResponse(
          "Correct validation code!"));
      return;
    }
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {

  }

  // Resends a validation email to the user
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    ValidationRequest vr = gson.fromJson(request.getReader(),
        ValidationRequest.class);

    if (!vr.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Information supplied was invalid"));
    }

    // Check the users password is correct
    UserResponse ur;
    try {
      ur = db.getUser(vr.getEmail());
    } catch (UserNotFoundException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "The email you entered was not found"));
      return;
    } catch (SQLException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "There was a problem with your request, please try again"));
      e.printStackTrace();
      return;
    } catch (InconsistentDataException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Something went wrong!"));
      e.printStackTrace();
      return;
    }
    try {
      if (!PasswordUtils.validatePassword(vr.getPassword(),
          ur.getHashedPassword())) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "You have provided an incorrect password"));
        return;
      }
    } catch (PasswordHashFailureException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Something went wrong, there is nothing you can do!"));
      e.printStackTrace();
      return;
    }

    // The users password is correct, construct a new validation code and update
    // the database
    CodeGenerator cg = new CodeGenerator();
    String newCode = cg.getCode(VALIDATION_CODE_LENGTH);
    try {
      db.updateUser(ur.getUserId(), newCode);
    } catch (SQLException | InconsistentDataException | UserNotFoundException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Something went wrong, please try again!"));
      e.printStackTrace();
      return;
    }

    // Send the user an email with the new code
    EmailUtils.sendValidationEmail(ur.getEmail(), newCode);

    request.setAttribute(Response.class.getSimpleName(), new SuccessResponse(
        "Please check your email for your validation code"));
  }
}
