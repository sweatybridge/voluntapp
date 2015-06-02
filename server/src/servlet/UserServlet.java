package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.RegisterRequest;
import req.UserRequest;
import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;
import resp.SuccessResponse;
import utils.EmailUtils;

import com.google.gson.Gson;

import db.CodeGenerator;
import db.DBInterface;
import exception.InconsistentDataException;
import exception.PasswordHashFailureException;
import exception.UserNotFoundException;

/**
 * Handles API requests to user resources.
 */
@WebServlet
public class UserServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Gson gson;
  private final DBInterface db;

  /**
   * Constructs a user servlet with injected dependencies.
   * 
   * @param gson
   *          json serialiser
   * @param db
   *          database interface
   */
  public UserServlet(Gson gson, DBInterface db) {
    this.gson = gson;
    this.db = db;
  }

  /**
   * Retrieve details of the current user.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {

    // Session should not be null if user is authenticated
    SessionResponse session = (SessionResponse) request
        .getAttribute(SessionResponse.class.getSimpleName());

    Response resp;
    try {
      int userId = session.getUserId();
      resp = db.getUser(new UserRequest(userId));
    } catch (UserNotFoundException e) {
      resp = new ErrorResponse("User is deleted but session is active.");
    } catch (SQLException | InconsistentDataException e) {
      resp = new ErrorResponse(e.getMessage());
    }

    request.setAttribute(Response.class.getSimpleName(), resp);
  }

  /**
   * TODO: Delete current user from the database.
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {

    // get current user id from auth token
    SessionResponse session = (SessionResponse) request
        .getAttribute(SessionResponse.class.getSimpleName());

    // delete from user table

    Response resp = new SuccessResponse(
        "Successfully deleted user from database.");

    request.setAttribute(Response.class.getSimpleName(), resp);
  }

  /**
   * Updates the user details with supplied information.
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {

    // get current user id from auth token
    SessionResponse session = (SessionResponse) request
        .getAttribute(SessionResponse.class.getSimpleName());

    // update user with new information

    Response resp = new SuccessResponse("Successfully updated user.");

    request.setAttribute(Response.class.getSimpleName(), resp);

  }

  /**
   * Registers the user with database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    // Parse user registration request
    RegisterRequest user = gson.fromJson(request.getReader(),
        RegisterRequest.class);

    // Validate registration
    if (!user.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "You have entered invalid registration information."));
      return;
    }

    try {
      // Write to database
      int userId = db.putUser(user);

      // Forward to session servlet
      // request.setAttribute("userId", userId);
      // getServletContext().getRequestDispatcher("/api/session").forward(request,
      // response);

      request.setAttribute(Response.class.getSimpleName(),
          new SuccessResponse());
    } catch (SQLException e) {
      e.printStackTrace();
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "The email you entered is already in use."));
    } catch (PasswordHashFailureException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Password Hashing Failed"));
    }
  }
}
