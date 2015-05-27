package servlet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;

import com.google.gson.Gson;

import db.DBInterface;

public abstract class ServletTest {

  protected static final int TEST_USER_ID = 7;
  protected static final String TEST_EMAIL = "james.bond@gmail.com";
  protected static final String TEST_PASSWORD = "123456";
  protected static final String TEST_PASSWORD_HASHED = "123456:123456";
  protected static final String TEST_PASSWORD_WRONG = "654321:342424";
  protected static final String TEST_FIRST_NAME = "James";
  protected static final String TEST_LAST_NAME = "Bond";

  protected static final String TEST_SESSION_ID = "111";

  @Mock
  protected HttpServletRequest req;
  @Mock
  protected HttpServletResponse resp;
  @Mock
  protected DBInterface db;
  @Mock
  protected ServletContext context;
  @Mock
  protected RequestDispatcher dispatcher;

  protected Gson gson = new Gson();

  @Before
  public void init() throws IOException {
    MockitoAnnotations.initMocks(this);

    // Post condition of authorization filter ensures that all requests handled
    // by servlets contain a valid session response object
    when(req.getAttribute(SessionResponse.class.getSimpleName())).thenReturn(
        new SessionResponse(TEST_SESSION_ID, TEST_USER_ID));
  }

  protected void validateErrorResponse() {
    // Check error response is installed
    verify(req).setAttribute(eq(Response.class.getSimpleName()),
        any(ErrorResponse.class));
  }

  protected void validateForwardingTo(String url) throws ServletException,
      IOException {
    // Check that request is forwarded to session servlet
    verify(context).getRequestDispatcher(url);
    verify(dispatcher).forward(req, resp);
  }

}
