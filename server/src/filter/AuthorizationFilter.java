package filter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;
import db.DBInterface;
import exception.SessionNotFoundException;

public class AuthorizationFilter implements Filter {

  private DBInterface db;

  public AuthorizationFilter(DBInterface db) {
    this.db = db;
  }

  @Override
  public void destroy() {
    /* Required by the filter interface. */
  }

  /**
   * Performs a database lookup of the session ID and adds an attribute to the
   * request which is equal to the result of the database query.
   */
  @Override
  public void doFilter(ServletRequest req, ServletResponse resp,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) req;

    // Allow user registration
    if (request.getRequestURI().equals("/api/user")
        && request.getMethod().equals("POST")) {
      // Attach a dummy session response
      req.setAttribute(SessionResponse.class.getSimpleName(),
          new SessionResponse());
      chain.doFilter(req, resp);
      return;
    }

    // Allow user login
    if (request.getRequestURI().equals("/api/session")
        && request.getMethod().equals("POST")) {
      // Attach a dummy session response
      req.setAttribute(SessionResponse.class.getSimpleName(),
          new SessionResponse());
      chain.doFilter(req, resp);
      return;
    }

    // Stop all requests without an auth token immediately
    String sessionID = request.getHeader("Authorization");
    if (sessionID == null) {
      HttpServletResponse response = (HttpServletResponse) resp;
      response.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
      return;
    }

    try {
      // Install session response on request chain
      SessionResponse sessionResp = db.getSession(sessionID);
      // TODO: refresh token

      req.setAttribute(SessionResponse.class.getSimpleName(), sessionResp);

    } catch (SessionNotFoundException e) {
      req.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Invalid session identifier - session identifier not found."));
    } catch (SQLException e) {
      req.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Database error while searching the session number."));
    } finally {
      chain.doFilter(req, resp);
    }

  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    /* Required by the interface. */
  }

}
