package servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Provide authentication to index.html and redirect to login when required.
 */
@WebServlet("")
public class DefaultServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Verify session id is installed
    for (Cookie c : request.getCookies()) {
      if (c.getName().equals("token")) {
        getServletContext().getNamedDispatcher("default").forward(request,
            response);
        return;
      }
    }

    response.sendRedirect("/login");
  }
}
