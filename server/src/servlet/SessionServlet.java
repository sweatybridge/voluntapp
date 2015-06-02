package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.UserRequest;
import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;
import resp.SuccessResponse;
import resp.UserResponse;
import utils.PasswordUtils;

import com.google.gson.Gson;

import db.DBInterface;
import db.SessionManager;
import exception.InconsistentDataException;
import exception.PasswordHashFailureException;
import exception.UserNotFoundException;

/**
 * Provide login and logout service.
 */
@WebServlet
public class SessionServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final Gson gson;
  private final DBInterface db;
  private final SessionManager sm;

  public SessionServlet(Gson gson, DBInterface db, SessionManager sm) {
    this.gson = gson;
    this.db = db;
    this.sm = sm;
  }

  /**
   * Creates a new session token based on user email and password.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Handle login by email and password
    Integer userId = (Integer) request.getAttribute("userId");
    if (userId == null) {

      // Parse user login request
      UserRequest login = gson.fromJson(request.getReader(), UserRequest.class);

      // Validate login request
      if (!login.isValid()) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "You have entered invalid login information."));
        return;
      }

      try {
        // Find the user in database
        UserResponse user = db.getUser(login);

        // Check that password matches the hashed value
        if (!PasswordUtils.validatePassword(login.getPassword(),
            user.getHashedPassword())) {
          request.setAttribute(Response.class.getSimpleName(),
              new ErrorResponse("You have entered invalid login information."));
          return;
        }

        // return user id for starting a session
        userId = user.getUserId();

      } catch (SQLException | InconsistentDataException
          | PasswordHashFailureException e) {
        e.printStackTrace();
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Something really bad has happened."));
        return;
      } catch (UserNotFoundException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "You have entered invalid login information."));
        return;
      }
    }

    try {
      
      // Start a new session
      String sessionId = sm.startSession(userId);
      SessionResponse loginResponse = new SessionResponse(sessionId);

      // Set the session cookie if request comes from a browser
      Cookie cookie = createSessionCookie(loginResponse);
      response.addCookie(cookie);

      // Pass response object to serialisation filter
      request.setAttribute(Response.class.getSimpleName(), loginResponse);

    } catch (SQLException e) {
      // Failed to start session
      request.setAttribute(Response.class.getSimpleName(),
          new ErrorResponse(e.getMessage()));
    }
  }

  /**
   * TODO: Refreshes the current session token.
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
  }

  /**
   * Logs out the user and remove its session.
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    SessionResponse session = (SessionResponse) request
        .getAttribute(SessionResponse.class.getSimpleName());

    Response resp = invalidateSession(session);

    request.setAttribute(Response.class.getSimpleName(), resp);
  }

  private Cookie createSessionCookie(Response resp) {
    /*
     * if (!"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
     * HttpSession jsession = request.getSession(); response.sendRedirect("/");
     * }
     */
    SessionResponse session = (SessionResponse) resp;
    Cookie cookie = new Cookie("token", session.getSessionId());
    cookie.setPath("/");
    // cookie.setHttpOnly(true);
    return cookie;
  }

  private Response invalidateSession(SessionResponse session) {
    if (sm.closeSession(session.getSessionId())) {
      return new SuccessResponse("You have successfully logged out.");
    } else {
      return new ErrorResponse("Unable to log out.");
    }
  }
}
