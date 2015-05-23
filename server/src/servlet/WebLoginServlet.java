package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import req.UserRequest;
import resp.ErrorResponse;
import resp.SessionResponse;
import resp.UserResponse;
import db.DBInterface;
import exception.InconsistentDataException;
import exception.SessionNotFoundException;
import exception.UserNotFoundException;

/**
 * Traditional web browser login for session persistence when accessed directly
 * from URL. (Using OAuth for XHR)
 */
@WebServlet
public class WebLoginServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final DBInterface db;

  public WebLoginServlet(DBInterface db) {
    this.db = db;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // if user already logged in, redirect to root
    HttpSession session = request.getSession(false);
    if (session != null) {
      response.sendRedirect("/");
    }
    // if user is not logged in, falls through to login static page
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Parse email and password
    String email = request.getParameter("email");
    String password = request.getParameter("password");
    
    UserRequest login = new UserRequest(email, password);

    try {
      UserResponse user = db.getUser(login);
    } catch (SQLException | UserNotFoundException | InconsistentDataException e1) {
    }
    
    HttpSession session = request.getSession(false);
    if (session != null) {

      // Verify session id
      String sessionId = session.getId();
      try {

        SessionResponse resp = db.getSession(sessionId);

        // serve application file
        getServletContext().getNamedDispatcher("default").forward(request,
            response);

      } catch (SQLException | SessionNotFoundException e) {
        response.sendRedirect("/login/");
      }
    }
    response.sendRedirect("/login/");
  }
}
