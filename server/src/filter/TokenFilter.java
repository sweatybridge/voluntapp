package filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provide authentication to index.html and redirect to login when required.
 */
@WebFilter("")
public class TokenFilter implements Filter {

  @Override
  public void destroy() {
    /* Required by filter interface */
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) req;

    for (Cookie c : request.getCookies()) {
      if (c.getName().equals("token")) {
        // Propagates request to the next filter
        chain.doFilter(req, resp);
        return;
      }
    }

    HttpServletResponse response = (HttpServletResponse) resp;
    response.sendRedirect("/login");
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
    /* Required by filter interface */
  }

}
