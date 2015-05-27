package servlet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import req.RegisterRequest;
import req.UserRequest;
import resp.Response;
import resp.UserResponse;

import com.google.common.collect.ImmutableMap;

import exception.InconsistentDataException;
import exception.PasswordHashFailureException;
import exception.UserNotFoundException;

public class UserServletTest extends ServletTest {

  private UserServlet servlet;

  @Before
  public void setUp() {
    // Create subclass to return mocked context
    servlet = new UserServlet(gson, db) {
      private static final long serialVersionUID = 1L;

      @Override
      public ServletContext getServletContext() {
        return context;
      }
    };
  }

  @Test
  public void getSucceedsWhenUserExistsInDatabase() throws SQLException,
      UserNotFoundException, InconsistentDataException {
    // Sets up expected response
    UserResponse expected =
        new UserResponse(TEST_EMAIL, TEST_PASSWORD, TEST_USER_ID,
            TEST_FIRST_NAME, TEST_LAST_NAME);

    // Sets up post condition of getUser
    when(db.getUser(any(UserRequest.class))).thenReturn(expected);

    // Method under test
    servlet.doGet(req, resp);

    // Verify pre condition of getUser
    ArgumentCaptor<UserRequest> arg =
        ArgumentCaptor.forClass(UserRequest.class);
    verify(db).getUser(arg.capture());
    assertEquals(TEST_USER_ID, arg.getValue().getUserId());

    // Check expected response is returned
    verify(req).setAttribute(Response.class.getSimpleName(), expected);
  }

  @Test
  public void getFailsWhenUserIsDeletedButSessionIsActive()
      throws SQLException, UserNotFoundException, InconsistentDataException {
    // Sets up post condition of getUser
    when(db.getUser(any(UserRequest.class))).thenThrow(
        new UserNotFoundException(TEST_EMAIL));

    // Method under test
    servlet.doGet(req, resp);

    // Verify pre condition of getUser
    ArgumentCaptor<UserRequest> arg =
        ArgumentCaptor.forClass(UserRequest.class);
    verify(db).getUser(arg.capture());
    assertEquals(TEST_USER_ID, arg.getValue().getUserId());

    validateErrorResponse();
  }

  @Test
  public void getFailsWhenDatabaseIsInconsistent() throws SQLException,
      UserNotFoundException, InconsistentDataException {
    // Sets up post condition of getUser
    when(db.getUser(any(UserRequest.class))).thenThrow(
        new InconsistentDataException(""));

    // Method under test
    servlet.doGet(req, resp);

    // Verify pre condition of getUser
    ArgumentCaptor<UserRequest> arg =
        ArgumentCaptor.forClass(UserRequest.class);
    verify(db).getUser(arg.capture());
    assertEquals(TEST_USER_ID, arg.getValue().getUserId());

    validateErrorResponse();
  }

  @Test
  public void postSucceedsUserRegistrationWhenInformationIsValid()
      throws IOException, SQLException, ServletException,
      PasswordHashFailureException {
    // Reader returns valid payload
    when(req.getReader()).thenReturn(
        new BufferedReader(new StringReader(gson.toJson(ImmutableMap.of(
            "email", TEST_EMAIL, "password", TEST_PASSWORD, "firstName",
            TEST_FIRST_NAME, "lastName", TEST_LAST_NAME)))));

    // Database returns valid user id
    when(db.putUser(any(RegisterRequest.class))).thenReturn(TEST_USER_ID);

    // Context returns mocked dispatcher
    when(context.getRequestDispatcher(any(String.class)))
        .thenReturn(dispatcher);

    // Method under test
    servlet.doPost(req, resp);

    // Checks user id is installed before forwarding
    verify(req).setAttribute("userId", TEST_USER_ID);

    validateForwardingTo("/session");
  }

  @Test
  public void postFailsRegistrationWhenRequestObjectIsInvalid()
      throws IOException, SQLException, ServletException,
      PasswordHashFailureException {
    // Reader returns valid payload
    when(req.getReader()).thenReturn(
        new BufferedReader(new StringReader(gson.toJson(ImmutableMap.of(
            "email", TEST_EMAIL, "password", TEST_PASSWORD)))));

    // Method under test
    servlet.doPost(req, resp);

    // Database is not accessed
    verify(db, never()).putUser(any(RegisterRequest.class));

    validateErrorResponse();
  }

  @Test
  public void postFailsRegistrationWhenEmailIsAlreadyInUse()
      throws IOException, SQLException, ServletException,
      PasswordHashFailureException {
    // Reader returns valid payload
    when(req.getReader()).thenReturn(
        new BufferedReader(new StringReader(gson.toJson(ImmutableMap.of(
            "email", TEST_EMAIL, "password", TEST_PASSWORD, "firstName",
            TEST_FIRST_NAME, "lastName", TEST_LAST_NAME)))));

    // Database throws exception for duplicate email
    when(db.putUser(any(RegisterRequest.class))).thenThrow(new SQLException());

    // Method under test
    servlet.doPost(req, resp);

    validateErrorResponse();
  }

  @Test
  public void putSucceedsUserUpdateWhenInformationIsValid() {
    // TODO: complete implementation
    servlet.doPut(req, resp);
  }

  @Test
  public void deleteSucceedsRemovingUserFromDatabase() {
    // TODO: complete implementation
    servlet.doDelete(req, resp);
  }

}
