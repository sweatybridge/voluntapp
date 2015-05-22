package servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import req.RegisterRequest;
import req.SessionRequest;
import req.UserRequest;
import resp.ErrorResponse;
import resp.LoginResponse;
import resp.UserResponse;

import com.google.gson.Gson;

import db.DBInterface;
import exception.UserNotFoundException;

public class UserServletTest {

  private static final Gson gson = new Gson();


  private static final int TEST_USER_ID = 3;

  /**
   * Test data to verify request objects.
   */
  private static final String TEST_EMAIL = "han_qiao@msn.com";
  private static final String TEST_PASSWORD = "123123";
  private static final String TEST_FIRST_NAME = "Qiao";
  private static final String TEST_LAST_NAME = "Han";

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
      when(db.addUser(any(RegisterRequest.class))).thenReturn(TEST_USER_ID);
      when(db.addSession(any(SessionRequest.class))).thenReturn(true);
    } catch (IOException | SQLException e1) {
      fail("Not yet implemented");
    }

    // Perform actual servlet operation
    try {
      servlet.doPut(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    // Check request object is created correctly
    ArgumentCaptor<RegisterRequest> arg0 =
        ArgumentCaptor.forClass(RegisterRequest.class);
    try {
      verify(db).addUser(arg0.capture());
    } catch (SQLException e) {
      fail("Not yet implemented");
    }
    RegisterRequest rr = arg0.getValue();
    assertEquals(TEST_EMAIL, rr.getEmail());
    assertEquals(TEST_PASSWORD, rr.getPassword());
    assertEquals(TEST_FIRST_NAME, rr.getFirstName());
    assertEquals(TEST_LAST_NAME, rr.getLastName());

    // Check session id is returned as token
    ArgumentCaptor<SessionRequest> arg1 =
        ArgumentCaptor.forClass(SessionRequest.class);
    try {
      verify(db).addSession(arg1.capture());
    } catch (SQLException e) {
      fail("Not yet implemented");
    }

    verify(respBody).print(
        gson.toJson(new LoginResponse(arg1.getValue().getSessionId())));
  }

  @Test
  public void doPutFailsRegistrationWhenEmailExistsInDatabase() {
    String payload =
        "payload=%7B%22email%22%3A%22han_qiao%40msn.com%22%2C%22password%22%3A%22123123%22%2C%22conf_password%22%3A%22123123%22%2C%22firstName%22%3A%22Qiao%22%2C%22lastName%22%3A%22Han%22%7D";

    // Sets up mock objects
    try {
      when(reqBody.readLine()).thenReturn(payload);
      when(db.addUser(any(RegisterRequest.class)))
          .thenThrow(new SQLException());
    } catch (IOException | SQLException e) {
      fail("Not yet implemented");
    }

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

    // Check that no data is inserted into database
    try {
      verify(db, never()).verifyUser(any(UserRequest.class));
    } catch (SQLException | UserNotFoundException e) {
      fail("Not yet implemented");
    }

    verify(resp).setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    verify(respBody).print(
        gson.toJson(new ErrorResponse("Error parsing request payload.")));
  }

  @Test
  public void doPostLogsInUserAndReturnsSessionID() {
    String payload = "{\"email\":\"han_qiao@msn.com\",\"password\":\"123123\"}";

    // Sets up mock objects
    when(req.getParameter("payload")).thenReturn(payload);
    try {
      when(db.verifyUser(any(UserRequest.class))).thenReturn(
          new UserResponse(TEST_EMAIL, TEST_PASSWORD, TEST_USER_ID));
      when(db.addSession(any(SessionRequest.class))).thenReturn(true);
    } catch (SQLException | UserNotFoundException e1) {
      fail("Not yet implemented");
    }

    // Perform actual servlet operation
    try {
      servlet.doPost(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    // Capture session id generated by SecureRandom
    ArgumentCaptor<SessionRequest> arg =
        ArgumentCaptor.forClass(SessionRequest.class);
    try {
      verify(db).addSession(arg.capture());
    } catch (SQLException e) {
      fail("Not yet implemented");
    }

    verify(respBody).print(
        gson.toJson(new LoginResponse(arg.getValue().getSessionId())));
  }

  @Test
  public void doPostFailsLogInWhenEmailOrPasswordIsWrong() {
    String payload = "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\"}";

    // Sets up mock objects
    when(req.getParameter("payload")).thenReturn(payload);
    try {
      when(db.verifyUser(any(UserRequest.class))).thenThrow(
          new UserNotFoundException(""));
    } catch (SQLException | UserNotFoundException e1) {
      fail("Not yet implemented");
    }

    // Perform actual servlet operation
    try {
      servlet.doPost(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    verify(resp).setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    verify(respBody).print(
        gson.toJson(new ErrorResponse("You have entered the wrong password.")));
  }

  @Test
  public void doPostWrapsExceptionInJsonResponseWhenPayloadParsingFails() {
    String payload = "invalid payload";

    // Sets up mock objects
    when(req.getParameter("payload")).thenReturn(payload);

    // Perform actual servlet operation
    try {
      servlet.doPost(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    // Check that no data is inserted into database
    try {
      verify(db, never()).verifyUser(any(UserRequest.class));
    } catch (SQLException | UserNotFoundException e) {
      fail("Not yet implemented");
    }

    verify(resp).setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    verify(respBody).print(
        gson.toJson(new ErrorResponse("Error parsing request payload.")));
  }
}
