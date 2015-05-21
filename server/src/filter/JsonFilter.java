package filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * Sets up JSON content type and UTF-8 char encoding for all API responses.
 * doFilter will be invoked on every incoming request.
 */
@WebFilter("/*")
public class JsonFilter implements Filter {

  @Override
  public void destroy() {
    /* Required by filter interface */
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp,
      FilterChain chain) throws IOException, ServletException {

    // Sets up content type and char encoding
    resp.setContentType("application/json");
    resp.setCharacterEncoding("utf-8");

    // Propagates request to the next filter
    chain.doFilter(req, resp);
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
    /* Required by filter interface */
  }

}
