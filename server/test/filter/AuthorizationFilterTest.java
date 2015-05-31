package filter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;

import javax.servlet.FilterChain;
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
import db.DBInterface;
import exception.SessionNotFoundException;

public class AuthorizationFilterTest {

  @Mock
  HttpServletRequest req;
  @Mock
  HttpServletResponse resp;
  @Mock
  FilterChain chain;
  @Mock
  DBInterface db;

  private static final String TEST_SESSION_ID = "123456";

  private AuthorizationFilter filter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    filter = new AuthorizationFilter(db);
  }

  @Test
  public void filterAllowsUnauthenticatedAccessToRegistrationPage()
      throws IOException, ServletException {
    // Set up request
    when(req.getRequestURI()).thenReturn("/api/user");
    when(req.getMethod()).thenReturn("POST");

    // Method under test
    filter.doFilter(req, resp, chain);

    // Fake session is installed
    verify(req).setAttribute(eq(SessionResponse.class.getSimpleName()),
        any(SessionResponse.class));
    verify(chain).doFilter(req, resp);
  }

  @Test
  public void filterAllowsUnauthenticatedAccessToLoginPage()
      throws IOException, ServletException {
    // Set up request
    when(req.getRequestURI()).thenReturn("/api/session");
    when(req.getMethod()).thenReturn("POST");

    // Method under test
    filter.doFilter(req, resp, chain);

    // Fake session is installed
    verify(req).setAttribute(eq(SessionResponse.class.getSimpleName()),
        any(SessionResponse.class));
    verify(chain).doFilter(req, resp);
  }

  @Test
  public void filterStopsRequestsWithoutAuthorizationHeaderImmediately()
      throws IOException, ServletException {
    // Method under test
    filter.doFilter(req, resp, chain);

    // Error status is returned without passing to json filter
    verify(resp).setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
    verify(chain, never()).doFilter(req, resp);
  }

  @Test
  public void filterReturnsAnErrorResponseWhenNoSessionIDWasFoundInBD()
      throws SQLException, SessionNotFoundException, IOException,
      ServletException {
    // Sets up request
    when(req.getHeader("Authorization")).thenReturn(TEST_SESSION_ID);
    when(db.getSession(TEST_SESSION_ID)).thenThrow(
        new SessionNotFoundException(TEST_SESSION_ID));

    // Method under test
    filter.doFilter(req, resp, chain);

    // Request propagates down filter chain
    verify(req, times(1)).setAttribute(eq(Response.class.getSimpleName()),
        any(ErrorResponse.class));
    verify(chain).doFilter(req, resp);
  }

  @Test
  public void filterReturnsAnErrorResponseWhenDBErrorOccurs()
      throws IOException, ServletException, SQLException,
      SessionNotFoundException {
    // Sets up request
    when(req.getHeader("Authorization")).thenReturn(TEST_SESSION_ID);
    when(db.getSession(TEST_SESSION_ID)).thenThrow(new SQLException());

    // Method under test
    filter.doFilter(req, resp, chain);

    // Request propagates down filter chain
    verify(req, times(1)).setAttribute(eq(Response.class.getSimpleName()),
        any(ErrorResponse.class));
    verify(chain).doFilter(req, resp);
  }

  @Test
  public void filterSearchesForSessionIDInDBAndAddsAnAttributeToRequest()
      throws SQLException, SessionNotFoundException, IOException,
      ServletException {
    SessionResponse session = new SessionResponse(TEST_SESSION_ID);

    // Sets up request
    when(req.getHeader("Authorization")).thenReturn(TEST_SESSION_ID);
    when(db.getSession(TEST_SESSION_ID)).thenReturn(session);

    // Method under test
    filter.doFilter(req, resp, chain);

    // Request propagates
    verify(req, times(1)).setAttribute(SessionResponse.class.getSimpleName(),
        session);
    verify(chain).doFilter(req, resp);
  }

}
