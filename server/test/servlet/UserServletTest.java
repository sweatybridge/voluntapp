package servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import req.RegisterRequest;
import req.UserRequest;
import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;
import resp.UserResponse;

import com.google.common.collect.ImmutableMap;

import exception.InconsistentDataException;
import exception.SessionNotFoundException;
import exception.UserNotFoundException;

public class UserServletTest extends ServletTest {

  private UserServlet servlet;

  @Before
  public void setUp() {
    // Create a new servlet for every test to prevent state persistence
    servlet = new UserServlet(gson, db);
  }

  @Test
  public void getReturnsUserDetails() {
    String token = TEST_SESSION_ID;
    UserResponse expected =
        new UserResponse(TEST_EMAIL, TEST_PASSWORD, TEST_USER_ID,
            TEST_FIRST_NAME, TEST_LAST_NAME);

    when(req.getHeader("Authorization")).thenReturn(token);
    try {
      when(db.getSession(token)).thenReturn(
          new SessionResponse(TEST_SESSION_ID, TEST_USER_ID));
      when(db.getUser(any(UserRequest.class))).thenReturn(expected);
    } catch (SQLException | UserNotFoundException | InconsistentDataException
        | SessionNotFoundException e1) {
      fail("Not yet implemented");
    }

    try {
      servlet.doGet(req, resp);
    } catch (IOException e) {
      fail("Not yet implemented");
    }

    ArgumentCaptor<UserRequest> arg =
        ArgumentCaptor.forClass(UserRequest.class);
    try {
      verify(db).getUser(arg.capture());
    } catch (SQLException | UserNotFoundException | InconsistentDataException e) {
      fail("Not yet implemented");
    }
    assertEquals(TEST_USER_ID, arg.getValue().getUserId());
  }

  @Test
  public void getFailsWhenTokenIsInvalid() throws SQLException,
      SessionNotFoundException, IOException {
    String token = "invalid";
    ErrorResponse expected = new ErrorResponse("Invalid authorization token.");

    when(req.getHeader("Authorization")).thenReturn(token);
    when(db.getSession(token)).thenThrow(new SessionNotFoundException());

    servlet.doGet(req, resp);
  }

  @Test
  public void postSucceedsUserRegistrationWhenInformationIsValid()
      throws IOException, SQLException, ServletException {
    // Reader returns valid payload
    when(req.getReader()).thenReturn(
        new BufferedReader(new StringReader(gson.toJson(ImmutableMap.of(
            "email", TEST_EMAIL, "password", TEST_PASSWORD, "firstName",
            TEST_FIRST_NAME, "lastName", TEST_LAST_NAME)))));

    // Database returns valid user id
    when(db.putUser(any(RegisterRequest.class))).thenReturn(TEST_USER_ID);

    // Method under test
    servlet.doPost(req, resp);

    // Checks user id is installed before forwarding
    verify(req).setAttribute("userId", TEST_USER_ID);
  }

  @Test
  public void postFailsRegistrationWhenEmailIsAlreadyInUse()
      throws IOException, SQLException, ServletException {
    // Reader returns valid payload
    when(req.getReader()).thenReturn(
        new BufferedReader(new StringReader(gson.toJson(ImmutableMap.of(
            "email", TEST_EMAIL, "password", TEST_PASSWORD, "firstName",
            TEST_FIRST_NAME, "lastName", TEST_LAST_NAME)))));

    // Database throws exception for duplicate email
    when(db.putUser(any(RegisterRequest.class))).thenThrow(new SQLException());

    // Method under test
    servlet.doPost(req, resp);

    // Error response is installed
    verify(req).setAttribute(eq(Response.class.getSimpleName()),
        any(ErrorResponse.class));
  }

  @Test
  public void putSucceedsUserUpdateWhenInformationIsValid() throws IOException {
    // TODO: complete implementation
    servlet.doPut(req, resp);
  }

  @Test
  public void deleteSucceedsRemovingUserFromDatabase() throws IOException {
    // TODO: complete implementation
    servlet.doDelete(req, resp);
  }

  @Test
  public void shouldForwardToSessionServletWhenRegistrationSucceeds()
      throws IOException {
    // TODO: complete implementation
    fail("Not yet implemented.");
  }

}
