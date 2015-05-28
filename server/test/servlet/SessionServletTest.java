package servlet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import req.UserRequest;
import resp.Response;
import resp.SessionResponse;
import resp.SuccessResponse;
import resp.UserResponse;
import utils.PasswordUtils;

import com.google.common.collect.ImmutableMap;

import db.SessionManager;
import exception.InconsistentDataException;
import exception.PasswordHashFailureException;
import exception.UserNotFoundException;

public class SessionServletTest extends ServletTest {

  @Mock
  protected SessionManager sm;

  private SessionServlet servlet;

  @Before
  public void setUp() {
    // Create a new servlet for every test to prevent state persistence
    servlet = new SessionServlet(gson, db, sm);
  }

  @Test
  public void postSucceedsLoginWhenEmailAndPasswordAreValid()
      throws IOException, SQLException, UserNotFoundException,
      InconsistentDataException, PasswordHashFailureException {

    prepareValidLogin();

    // Method under test
    servlet.doPost(req, resp);

    // Check correct session is installed on request
    ArgumentCaptor<SessionResponse> attr =
        ArgumentCaptor.forClass(SessionResponse.class);
    verify(req)
        .setAttribute(eq(Response.class.getSimpleName()), attr.capture());
    SessionResponse session = attr.getValue();

    assertEquals(TEST_SESSION_ID, session.getSessionId());
  }

  @Test
  public void postSucceedsLoginWhenUserIdWasForwarded() throws IOException,
      SQLException, UserNotFoundException, InconsistentDataException {
    // UserId is installed on request
    when(req.getAttribute("userId")).thenReturn(TEST_USER_ID);

    // Session manager returns valid session
    when(sm.startSession(TEST_USER_ID)).thenReturn(TEST_SESSION_ID);

    // Method under test
    servlet.doPost(req, resp);

    // No need to fetch user from database
    verify(db, never()).getUser(any(UserRequest.class));

    // Check correct session is installed on request
    ArgumentCaptor<SessionResponse> attr =
        ArgumentCaptor.forClass(SessionResponse.class);
    verify(req)
        .setAttribute(eq(Response.class.getSimpleName()), attr.capture());
    SessionResponse session = attr.getValue();

    assertEquals(TEST_SESSION_ID, session.getSessionId());

    validateCookie();
  }

  @Test
  public void postFailsLoginWhenForwardedRequestCannotStartSession()
      throws IOException, SQLException, UserNotFoundException,
      InconsistentDataException {
    // UserId is installed on request
    when(req.getAttribute("userId")).thenReturn(TEST_USER_ID);

    // Session manager fails to insert into database
    when(sm.startSession(any(Integer.class))).thenThrow(new SQLException());

    // Method under test
    servlet.doPost(req, resp);

    validateErrorResponse();
  }

  @Test
  public void postFailsLoginWhenEmailIsNotFound() throws IOException,
      SQLException, UserNotFoundException, InconsistentDataException {
    // Reader returns valid payload
    when(req.getReader()).thenReturn(
        new BufferedReader(new StringReader(gson.toJson(ImmutableMap.of(
            "email", TEST_EMAIL, "password", TEST_PASSWORD)))));

    // Database throws exception
    when(db.getUser(any(UserRequest.class))).thenThrow(
        new UserNotFoundException(TEST_EMAIL));

    // Method under test
    servlet.doPost(req, resp);

    // No session is created
    verify(sm, never()).startSession(any(Integer.class));

    validateErrorResponse();
  }

  @Test
  public void postFailsLoginWhenPasswordIsWrong() throws IOException,
      SQLException, UserNotFoundException, InconsistentDataException {
    // Reader returns valid payload
    when(req.getReader()).thenReturn(
        new BufferedReader(new StringReader(gson.toJson(ImmutableMap.of(
            "email", TEST_EMAIL, "password", TEST_PASSWORD)))));

    // Database returns valid user
    when(db.getUser(any(UserRequest.class))).thenReturn(
        new UserResponse(TEST_EMAIL, TEST_PASSWORD_WRONG, TEST_USER_ID,
            TEST_FIRST_NAME, TEST_LAST_NAME));

    // Method under test
    servlet.doPost(req, resp);

    // No session is created
    verify(sm, never()).startSession(any(Integer.class));

    validateErrorResponse();
  }

  @Test
  public void postFailsLoginWhenSessionCannotBeCreatedInDatabase()
      throws IOException, SQLException, UserNotFoundException,
      InconsistentDataException {
    // Reader returns valid payload
    when(req.getReader()).thenReturn(
        new BufferedReader(new StringReader(gson.toJson(ImmutableMap.of(
            "email", TEST_EMAIL, "password", TEST_PASSWORD)))));

    // Database returns valid user
    when(db.getUser(any(UserRequest.class))).thenReturn(
        new UserResponse(TEST_EMAIL, TEST_PASSWORD_HASHED, TEST_USER_ID,
            TEST_FIRST_NAME, TEST_LAST_NAME));

    // Session manager fails to insert into database
    when(sm.startSession(any(Integer.class))).thenThrow(new SQLException());

    // Method under test
    servlet.doPost(req, resp);

    validateErrorResponse();
  }

  @Test
  public void postFailsLoginWhenRequestObjectIsInvalid() throws IOException,
      SQLException, UserNotFoundException, InconsistentDataException {
    // Reader returns invalid payload
    when(req.getReader()).thenReturn(
        new BufferedReader(new StringReader(gson.toJson(ImmutableMap.of(
            "email", TEST_EMAIL)))));

    // Method under test
    servlet.doPost(req, resp);

    // Database and session manager are never accessed
    verify(db, never()).getUser(any(UserRequest.class));
    verify(sm, never()).startSession(any(Integer.class));

    validateErrorResponse();
  }

  @Test
  public void putSucceedsTokenRefresh() throws IOException {
    // TODO: complete implementation
    servlet.doPut(req, resp);
  }

  @Test
  public void deleteSucceedsLogoutWhenAuthorizationHeaderIsValid()
      throws IOException {
    // Header contains valid session id
    when(req.getHeader("Authorization")).thenReturn(TEST_SESSION_ID);

    // Session manager successfully closes session
    when(sm.closeSession(TEST_SESSION_ID)).thenReturn(true);

    // Method under test
    servlet.doDelete(req, resp);

    // Success response is installed as attribute
    verify(req).setAttribute(eq(Response.class.getSimpleName()),
        any(SuccessResponse.class));
  }

  @Test
  public void deleteFailsLogoutWhenSessionCannotBeClosed() throws IOException {
    // Header contains valid session id
    when(req.getHeader("Authorization")).thenReturn(TEST_SESSION_ID);

    // Session manager successfully closes session
    when(sm.closeSession(TEST_SESSION_ID)).thenReturn(false);

    // Method under test
    servlet.doDelete(req, resp);

    // Success response is installed as attribute
    verify(req).setAttribute(eq(Response.class.getSimpleName()),
        any(SuccessResponse.class));
  }

  @Test
  public void shouldAddCookieWhenLoginSucceeds() throws IOException,
      SQLException, UserNotFoundException, InconsistentDataException,
      PasswordHashFailureException {

    prepareValidLogin();

    // Method under test
    servlet.doPost(req, resp);

    validateCookie();
  }

  private void prepareValidLogin() throws IOException, SQLException,
      UserNotFoundException, InconsistentDataException,
      PasswordHashFailureException {
    // Reader returns valid payload
    when(req.getReader()).thenReturn(
        new BufferedReader(new StringReader(gson.toJson(ImmutableMap.of(
            "email", TEST_EMAIL, "password", TEST_PASSWORD)))));

    // Database returns valid user
    when(db.getUser(any(UserRequest.class))).thenReturn(
        new UserResponse(TEST_EMAIL, PasswordUtils
            .getPasswordHash(TEST_PASSWORD), TEST_USER_ID, TEST_FIRST_NAME,
            TEST_LAST_NAME));

    // Session manager returns valid session
    when(sm.startSession(TEST_USER_ID)).thenReturn(TEST_SESSION_ID);
  }

  private void validateCookie() {
    // Check that cookie is installed on response
    ArgumentCaptor<Cookie> arg = ArgumentCaptor.forClass(Cookie.class);
    verify(resp).addCookie(arg.capture());
    Cookie cookie = arg.getValue();

    assertEquals("token", cookie.getName());
    assertEquals(TEST_SESSION_ID, cookie.getValue());
  }

}
