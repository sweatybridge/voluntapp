package filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;
import resp.SuccessResponse;

import com.google.gson.Gson;

public class JsonFilterTest {

  @Mock
  private HttpServletRequest req;
  @Mock
  private HttpServletResponse resp;
  @Mock
  private FilterChain chain;
  @Mock
  private FilterConfig config;

  private final Gson gson = new Gson();
  private JsonFilter filter;
  private StringWriter output;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    filter = new JsonFilter(gson);
    output = new StringWriter();
  }

  @Test
  public void destroyDoesNothing() {
    filter.destroy();
  }

  @Test
  public void filterReturnsErrorWhenSessionIsNotSet() throws IOException,
      ServletException {
    ErrorResponse expected = new ErrorResponse("Session missing.");

    prepareResponse(expected);

    // Method under test
    filter.doFilter(req, resp, chain);

    validateContentTypeAndCharEncoding();

    // Check that chain does not propagate further
    verify(chain, never()).doFilter(req, resp);

    // 400 status code is set
    verify((HttpServletResponse) resp).setStatus(
        HttpURLConnection.HTTP_BAD_REQUEST);

    // Verify output is correctly serialised
    assertEquals(gson.toJson(expected), output.toString());
  }

  @Test
  public void filterReturnsServletResponseWhenSessionIsSet()
      throws IOException, ServletException {
    SuccessResponse expected =
        new SuccessResponse("Successfully handled request.");

    prepareResponse(expected);

    // Session is set
    when(req.getAttribute(SessionResponse.class.getSimpleName())).thenReturn(
        new SessionResponse());

    // Method under test
    filter.doFilter(req, resp, chain);

    validateContentTypeAndCharEncoding();

    // Check that filter chain is invoked
    verify(chain).doFilter(req, resp);

    // Check tatus code is unchanged
    verify((HttpServletResponse) resp, never()).setStatus(any(Integer.class));

    assertEquals(gson.toJson(expected), output.toString());
  }

  @Test
  public void initDoesNothing() throws ServletException {
    filter.init(config);
  }

  private void prepareResponse(Response expected) throws IOException {
    // getWriter returns verifiable string writer
    when(resp.getWriter()).thenReturn(new PrintWriter(output));
    when(req.getAttribute(Response.class.getSimpleName())).thenReturn(expected);
  }

  private void validateContentTypeAndCharEncoding() {
    verify(resp).setContentType("application/json");
    verify(resp).setCharacterEncoding("utf-8");
  }

}
