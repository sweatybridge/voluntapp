package servlet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;

import resp.SessionResponse;
import exception.SessionNotFoundException;

public class DefaultServletTest extends ServletTest {

  private DefaultServlet servlet;

  @Before
  public void setUp() {
    servlet = new DefaultServlet(db);
  }

  @Test
  public void getForwardsToMainWhenCookieContainsValidSession()
      throws SQLException, SessionNotFoundException, IOException,
      ServletException {
    // Sets up mock
    prepareClientCookies();

    // Database returns valid session
    when(db.getSession(any(String.class))).thenReturn(
        new SessionResponse(TEST_SESSION_ID));

    // Method under test
    servlet.doGet(req, resp);

    // TODO: validate forwarding
  }

  @Test
  public void getForwardsToLoginWhenSessionIsInvalid() throws SQLException,
      SessionNotFoundException, IOException, ServletException {
    // Sets up mock
    prepareClientCookies();

    // Database contains no session
    when(db.getSession(any(String.class))).thenThrow(
        new SessionNotFoundException());

    // Method under test
    servlet.doGet(req, resp);

    // TODO: validate forwarding
  }

  @Test
  public void getForwardsToLoginWhenCookieIsNotSet() throws SQLException,
      SessionNotFoundException, IOException, ServletException {
    // Return immediately without querying database
    verify(db, never()).getSession(any(String.class));

    // Method under test
    servlet.doGet(req, resp);

    // TODO: validate forwarding
  }

  private void prepareClientCookies() {
    Cookie[] cookies = new Cookie[1];
    cookies[0] = new Cookie("token", TEST_SESSION_ID);
    when(req.getCookies()).thenReturn(cookies);
  }
}
