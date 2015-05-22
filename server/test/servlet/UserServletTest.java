package servlet;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import resp.ErrorResponse;
import resp.LoginResponse;

import com.google.gson.Gson;

import db.DBInterface;
import db.SQLInsert;

public class UserServletTest {

  private static final Gson gson = new Gson();

  private static final String TEST_SESSION_ID = "test session id";

  @Mock
  private DBInterface db;
  @Mock
  private HttpServletRequest req;
  @Mock
  private HttpServletResponse resp;
  @Mock
  private BufferedReader reqBody;
  @Mock
  private PrintWriter respBody;

  private UserServlet servlet;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    // Create a new servlet for every test to prevent state persistence
    servlet = new UserServlet(gson, db);

    // Sets up mocks for buffered reader and writer
    try {
      when(resp.getWriter()).thenReturn(respBody);
      when(req.getReader()).thenReturn(reqBody);
    } catch (IOException e) {
      fail("Not yet implemented");
    }
  }

  @Test
  public void testDoGetHttpServletRequestHttpServletResponse() {
    fail("Not yet implemented");
  }

  @Test
  public void testDoDeleteHttpServletRequestHttpServletResponse() {
    fail("Not yet implemented");
  }

  @Test
  public void doPutRegistersUserInDatabase() {
    String payload =
        "payload=%7B%22email%22%3A%22han_qiao%40msn.com%22%2C%22password%22%3A%22123123%22%2C%22conf_password%22%3A%22123123%22%2C%22firstName%22%3A%22Qiao%22%2C%22lastName%22%3A%22Han%22%7D";

    // Sets up mock objects
    try {
      when(reqBody.readLine()).thenReturn(payload);
    } catch (IOException e1) {
      fail("Not yet implemented");
    }
    when(db.insert(any(SQLInsert.class))).thenReturn(true);

    // Perform actual servlet operation
    try {
      servlet.doPut(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    verify(db).insert(any(SQLInsert.class));
    verify(respBody).print(gson.toJson(new LoginResponse(TEST_SESSION_ID)));
  }

  @Test
  public void doPutFailsRegistrationWhenEmailExistsInDatabase() {
    String payload =
        "payload=%7B%22email%22%3A%22han_qiao%40msn.com%22%2C%22password%22%3A%22123123%22%2C%22conf_password%22%3A%22123123%22%2C%22firstName%22%3A%22Qiao%22%2C%22lastName%22%3A%22Han%22%7D";

    // Sets up mock objects
    try {
      when(reqBody.readLine()).thenReturn(payload);
    } catch (IOException e1) {
      fail("Not yet implemented");
    }
    when(db.insert(any(SQLInsert.class))).thenReturn(false);

    // Perform actual servlet operation
    try {
      servlet.doPut(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    verify(resp).setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    verify(respBody).print(
        gson.toJson(new ErrorResponse(
            "The email you entered is already in use.")));
  }

  @Test
  public void doPutWrapsExceptionInJsonResponseWhenPayloadParsingFails() {
    String payload = "invalid payload";

    // Sets up mock objects
    try {
      when(reqBody.readLine()).thenReturn(payload);
    } catch (IOException e1) {
      fail("Not yet implemented");
    }

    // Perform actual servlet operation
    try {
      servlet.doPut(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    verify(db, never()).insert(any(SQLInsert.class));
    verify(resp).setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    verify(respBody).print(
        gson.toJson(new ErrorResponse("Error parsing request payload.")));
  }

  @Test
  public void doPostLogsInUserAndReturnsSessionID() {
    String payload = "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\"}";

    // Sets up mock objects
    when(req.getParameter("payload")).thenReturn(payload);

    // Perform actual servlet operation
    try {
      servlet.doPost(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    verify(respBody).print(gson.toJson(new LoginResponse(TEST_SESSION_ID)));
  }

  @Test
  public void doPostFailsLogInWhenPasswordIsWrong() {
    String payload = "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\"}";

    // Sets up mock objects
    when(req.getParameter("payload")).thenReturn(payload);

    // Perform actual servlet operation
    try {
      servlet.doPost(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    verify(respBody).print(gson.toJson(new LoginResponse(TEST_SESSION_ID)));
  }

  @Test
  public void doPostFailsLogInWhenEmailIsNotFound() {
    String payload = "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\"}";

    // Sets up mock objects
    when(req.getParameter("payload")).thenReturn(payload);

    // Perform actual servlet operation
    try {
      servlet.doPost(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    verify(respBody).print(gson.toJson(new LoginResponse(TEST_SESSION_ID)));
  }

  @Test
  public void doPosttWrapsExceptionInJsonResponseWhenPayloadParsingFails() {
    String payload = "invalid payload";

    // Sets up mock objects
    when(req.getParameter("payload")).thenReturn(payload);

    // Perform actual servlet operation
    try {
      servlet.doPost(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    verify(db, never()).insert(any(SQLInsert.class));
    verify(resp).setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    verify(respBody).print(
        gson.toJson(new ErrorResponse("Error parsing request payload.")));
  }
}
