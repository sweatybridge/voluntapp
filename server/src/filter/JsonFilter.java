package filter;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import resp.ErrorResponse;
import resp.Response;

import com.google.gson.Gson;

/**
 * Sets up JSON content type and UTF-8 char encoding for all API responses.
 * Serialises Response objects to JSON. doFilter will be invoked on every
 * incoming request.
 */
@WebFilter("/*")
public class JsonFilter implements Filter {

  private Gson gson;

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

    // Retrieve response object installed by servlet
    Response servletResponse =
        (Response) req.getAttribute(Response.class.getSimpleName());

    // Handles responses in a RESTful manner
    if (servletResponse instanceof ErrorResponse) {
      HttpServletResponse response = (HttpServletResponse) resp;
      response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    // TODO: Remove this legacy null check
    if (servletResponse != null) {
      gson.toJson(servletResponse, resp.getWriter());
    }
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
    /* Required by filter interface */
    this.gson = new Gson();
  }

}
