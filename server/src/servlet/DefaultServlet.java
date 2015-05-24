package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import db.DBInterface;
import exception.SessionNotFoundException;

/**
 * Provide authentication to main.html and redirect to login when required.
 */
@WebServlet
public class DefaultServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final DBInterface db;

  public DefaultServlet(DBInterface db) {
    this.db = db;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Verify session id is installed
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie c : cookies) {
        if (c.getName().equals("token")) {

          // Validate session id with database
          String sid = c.getValue();
          try {
            db.getSession(sid);
            getServletContext().getRequestDispatcher("/WEB-INF/main.html")
                .forward(request, response);
            return;
          } catch (SQLException | SessionNotFoundException e) {
          }
        }
      }
    }

    // Forward to login page
    getServletContext().getRequestDispatcher("/index.html").forward(request,
        response);
  }
}
