package servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;

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

import com.google.gson.Gson;

import db.DBInterface;
import exception.InconsistentDataException;
import exception.SessionNotFoundException;
import exception.UserNotFoundException;

/**
 * Handles API requests to user resources.
 */
@WebServlet
public class UserServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  // private static final Logger logger = Logger.getLogger("UserServlet");

  private final Gson gson;
  private final DBInterface db;

  /**
   * Constructs a user servlet with injected dependencies.
   * 
   * @param gson json serialiser
   * @param db database interface
   */
  public UserServlet(Gson gson, DBInterface db) {
    this.gson = gson;
    this.db = db;
  }

  /**
   * TODO: Retrieve details of the current user.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Parse authorization header
    String auth = request.getHeader("Authorization");

    Response resp = handle(auth);
    if (resp instanceof ErrorResponse) {
      response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    response.getWriter().print(gson.toJson(resp));
  }

  /**
   * TODO: Delete current user from the database.
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    // get current user id from auth token
    // delete from user table

    Response resp =
        new SuccessResponse("Successfully deleted user from database.");

    response.getWriter().print(gson.toJson(resp));
  }

  /**
   * TODO: Updates the user details with supplied information.
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {}

  /**
   * Registers the user with database.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    // Parse user registration request
    RegisterRequest user =
        gson.fromJson(request.getReader(), RegisterRequest.class);

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
      request.setAttribute("userId", userId);
      getServletContext().getRequestDispatcher("/session").forward(request,
          response);

    } catch (SQLException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "The email you entered is already in use."));
    }
  }

  private Response handle(String auth) {
    try {
      // TODO: improve security against brute force attack
      SessionResponse session = db.getSession(auth);

      return db.getUser(new UserRequest(session.getUserId()));
    } catch (SessionNotFoundException e) {
      return new ErrorResponse("Invalid authorization token.");
    } catch (UserNotFoundException | InconsistentDataException e) {
      return new ErrorResponse("User does not exist in database.");
    } catch (SQLException e) {
      return new ErrorResponse("Database error.");
    }
  }
}
