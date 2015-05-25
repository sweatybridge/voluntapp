package filter;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import resp.ErrorResponse;
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

  /*
   * Performs a database lookup of the session ID and adds an attribute to the
   * request which is equal to the result of the database query.
   */
  @Override
  public void doFilter(ServletRequest req, ServletResponse resp,
      FilterChain chain) throws IOException, ServletException {

    String sessionID = ((HttpServletRequest) req).getHeader("Authorization");
    try {
      SessionResponse sessionResp = db.getSession(sessionID);
      req.setAttribute(SessionResponse.class.getSimpleName(), sessionResp);
    } catch (SessionNotFoundException e) {
      req.setAttribute(SessionResponse.class.getSimpleName(),
          new ErrorResponse("Invalid session identifier - session "
              + "identifier not found."));
    } catch (SQLException e) {
      req.setAttribute(SessionResponse.class.getSimpleName(),
          new ErrorResponse("Database error while searching the "
              + "session number."));
    }

    chain.doFilter(req, resp);
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    /* Required by the interface. */
  }

}
