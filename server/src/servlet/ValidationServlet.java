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

/**
 * End point to handle email validation requests. It is used to validate
 * accounts created.
 * 
 * @author nc1813
 * 
 */
public class ValidationServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private final DBInterface db;
  private final Gson gson;
  private final CodeGenerator cg;

  public static final int VALIDATION_CODE_LENGTH = 20;

  public ValidationServlet(Gson gson, DBInterface db, CodeGenerator cg) {
    this.db = db;
    this.gson = gson;
    this.cg = cg;
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
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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

  // Creates a temporary password and emails it to the user
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    ValidationRequest vr = gson.fromJson(request.getReader(),
        ValidationRequest.class);

    // Set up the type for the response as we do not go through the filers
    response.setContentType("application/json");
    response.setCharacterEncoding("utf-8");

    if (vr.getEmail() == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Information supplied was invalid"));
      return;
    }

    // Create a new temp password for the user
    String hashedPassword, password;
    try {
      password = cg.getCode(PasswordUtils.TEMP_PASSWORD_LENGTH);
      hashedPassword = PasswordUtils.getPasswordHash(password);
    } catch (PasswordHashFailureException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Something went wrong, please try again"));
      e.printStackTrace();
      return;
    }

    // Update the users password with the new one
    try {

      db.updateUser(vr.getEmail(), hashedPassword);
    } catch (InconsistentDataException | SQLException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Something went wrong, please try again"));
      e.printStackTrace();
      return;
    } catch (UserNotFoundException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      Response r = new ErrorResponse("The email you provided does not exist");
      request.setAttribute(Response.class.getSimpleName(), r);
      gson.toJson(r, response.getWriter());
      return;
    }

    // Email the user with the new password
    EmailUtils.sendTempPassword(vr.getEmail(), password);

    SuccessResponse s = new SuccessResponse(
        "Email with a new password has been sent");
    request.setAttribute(Response.class.getSimpleName(), s);
    gson.toJson(s, response.getWriter());

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
      return;
    }

    // Check the users password is correct
    UserResponse ur;
    try {
      ur = db.getUser(vr.getEmail());
    } catch (UserNotFoundException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "The email you entered was not found"));
      return;
    } catch (SQLException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "There was a problem with your request, please try again"));
      e.printStackTrace();
      return;
    } catch (InconsistentDataException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Something went wrong!"));
      e.printStackTrace();
      return;
    }
    try {
      if (!PasswordUtils.validatePassword(vr.getPassword(),
          ur.getHashedPassword())) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "You have provided an incorrect password"));
        return;
      }
    } catch (PasswordHashFailureException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Something went wrong, there is nothing you can do!"));
      e.printStackTrace();
      return;
    }

    // The users password is correct, construct a new validation code and update
    // the database
    String newCode = cg.getCode(VALIDATION_CODE_LENGTH);
    try {
      db.updateUser(ur.getUserId(), newCode);
    } catch (SQLException | InconsistentDataException | UserNotFoundException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Something went wrong, please try again!"));
      e.printStackTrace();
      return;
    }

    // Send the user an email with the new code
    EmailUtils.sendValidationEmail(ur.getEmail(), newCode);

    SuccessResponse s = new SuccessResponse(
        "Please check your email for your validation code");
    response.setContentType("application/json");
    response.setCharacterEncoding("utf-8");
    request.setAttribute(Response.class.getSimpleName(), s);
    gson.toJson(s, response.getWriter());
  }
}
