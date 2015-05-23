package servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.RegisterRequest;
import req.UserRequest;
import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;
import resp.SuccessResponse;
import resp.UserResponse;

import com.google.gson.Gson;

import db.DBInterface;
import db.SessionManager;
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
  private final SessionManager sm;

  /**
   * Constructs a user servlet with injected dependencies.
   * 
   * @param gson json serialiser
   * @param db database interface
   */
  public UserServlet(Gson gson, DBInterface db) {
    this.gson = gson;
    this.db = db;
    this.sm = new SessionManager(db);
  }

  /**
   * Retrieve details of the current user.
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
   * Delete current user from the database.
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    // TODO get current user id from auth token
    // TODO delete from user table

    Response resp =
        new SuccessResponse("Successfully deleted user from database.");

    response.getWriter().print(gson.toJson(resp));
  }

  /**
   * Register a new user with supplied information.
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Parse user register request
    RegisterRequest req;
    try {
      String body =
          request.getReader().readLine().substring("payload=".length());
      String payload = URLDecoder.decode(body, "UTF-8");
      req = gson.fromJson(payload, RegisterRequest.class);
    } catch (IOException | RuntimeException e) {
      req = RegisterRequest.INVALID;
    }

    // Handle request in a RESTful manner
    Response resp = handle(req);
    if (resp instanceof ErrorResponse) {
      response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    // Write response to output
    response.getWriter().print(gson.toJson(resp));
  }

  /**
   * Logs in the user with email and password.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Parse user login request
    String data = request.getParameter("payload");
    UserRequest req;
    try {
      req = gson.fromJson(data, UserRequest.class);
    } catch (RuntimeException e) {
      req = UserRequest.INVALID;
    }

    // Handle request in a RESTful manner
    Response resp = handle(req);
    if (resp instanceof ErrorResponse) {
      response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    } else {
      // Initiate jsession if request is not ajax
      /*
      if (!"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
        HttpSession jsession = request.getSession();
        response.sendRedirect("/");
      }
      */
      SessionResponse session = (SessionResponse) resp;
      request.getSession().setAttribute("token", session.getSessionId());
      // Cookie cookie = new Cookie("token", session.getSessionId());
      // cookie.setHttpOnly(true);
      // response.addCookie(cookie);
    }

    // Write response to output
    response.getWriter().print(gson.toJson(resp));
  }

  private Response handle(RegisterRequest req) {
    if (req == RegisterRequest.INVALID) {
      return new ErrorResponse("Error parsing request payload.");
    }

    // Validate registration
    if (!req.isValid()) {
      return new ErrorResponse(
          "You have entered invalid registration information.");
    }

    // Write to database
    try {
      int userId = db.putUser(req);
      String token = sm.startSession(userId);

      // Successfully registered
      return new SessionResponse(token);
    } catch (SQLException e) {
      return new ErrorResponse("The email you entered is already in use.");
    }
  }

  private Response handle(UserRequest req) {
    if (req == UserRequest.INVALID) {
      return new ErrorResponse("Error parsing request payload.");
    }

    // Validate login
    if (!req.isValid()) {
      return new ErrorResponse("You have entered invalid login information.");
    }

    try {
      UserResponse user = db.getUser(req);

      // TODO: Check that password matches the hashed value
      if (!req.getPassword().equals(user.getHashedPassword())) {
        return new ErrorResponse("You have entered a wrong password.");
      }

      // Start a new session to support multi-client login
      String token = sm.startSession(user.getUserId());

      // Successfully logged in
      return new SessionResponse(token);
    } catch (SQLException | UserNotFoundException | InconsistentDataException e) {
      return new ErrorResponse("You have entered a wrong password.");
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
