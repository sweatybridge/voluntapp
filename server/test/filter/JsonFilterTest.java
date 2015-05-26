package filter;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.Gson;

public class JsonFilterTest {

  @Mock
  private HttpServletRequest req;
  @Mock
  private HttpServletResponse resp;
  @Mock
  private FilterChain chain;

  private final Gson gson = new Gson();
  private JsonFilter filter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    filter = new JsonFilter(gson);
  }

  @Test
  public void destroyDoesNothing() {}

  @Test
  public void doFilterSetsContentTypeAndCharEncoding() {
    try {
      filter.doFilter(req, resp, chain);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    verify(resp).setContentType("application/json");
    verify(resp).setCharacterEncoding("utf-8");
  }

  @Test
  public void initDoesNothing() {}

}
