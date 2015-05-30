package servlet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;

import exception.SessionNotFoundException;

public class DefaultServletTest extends ServletTest {

  private DefaultServlet servlet;

  @Before
  public void setUp() {
    servlet = new DefaultServlet(db) {
      private static final long serialVersionUID = 1L;

      @Override
      public ServletContext getServletContext() {
        return context;
      }
    };
  }

  @Test
  public void getForwardsToMainWhenCookieContainsValidSession()
      throws SQLException, SessionNotFoundException, IOException,
      ServletException {

    prepareClientCookies();

    // Context returns mocked dispatcher
    when(context.getRequestDispatcher(any(String.class)))
        .thenReturn(dispatcher);

    // Method under test
    servlet.doGet(req, resp);

    // Database returns valid session
    verify(db).getSession(TEST_SESSION_ID);

    validateForwardingTo("/WEB-INF/main.html");
  }

  @Test
  public void getForwardsToLoginWhenSessionIsInvalid() throws SQLException,
      SessionNotFoundException, IOException, ServletException {

    prepareClientCookies();

    // Database contains no session
    when(db.getSession(any(String.class))).thenThrow(
        new SessionNotFoundException(TEST_SESSION_ID));

    // Context returns mocked dispatcher
    when(context.getRequestDispatcher(any(String.class)))
        .thenReturn(dispatcher);

    // Method under test
    servlet.doGet(req, resp);

    validateForwardingTo("/index.html");
  }

  @Test
  public void getForwardsToLoginWhenCookieIsNotSet() throws SQLException,
      SessionNotFoundException, IOException, ServletException {

    // Context returns mocked dispatcher
    when(context.getRequestDispatcher(any(String.class)))
        .thenReturn(dispatcher);

    // Method under test
    servlet.doGet(req, resp);

    // Return immediately without querying database
    verify(db, never()).getSession(any(String.class));

    validateForwardingTo("/index.html");
  }

  private void prepareClientCookies() {
    Cookie[] cookies = new Cookie[1];
    cookies[0] = new Cookie("token", TEST_SESSION_ID);
    when(req.getCookies()).thenReturn(cookies);
  }
}
