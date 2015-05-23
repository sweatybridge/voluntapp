package db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import exception.InconsistentDataException;
import exception.UserNotFoundException;

import req.RegisterRequest;
import req.SessionRequest;
import req.UserRequest;
import resp.UserResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class DBInterfaceTest {

  @Mock
  private Connection conn;

  @Mock
  private Statement stmt;

  @Mock
  private ResultSet rs;

  private DBInterface db;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    db = new DBInterface(conn);
  }

  // Database Columns
  public final static String EMAIL_COLUMN = "EMAIL";
  public final static String PASSWORD_COLUMN = "PASSWORD";
  public final static String ID_COLUMN = "ID";
  public final static String FIRST_NAME_COLUMN = "FIRSTNAME";
  public final static String LAST_NAME_COLUMN = "LASTNAME";

  // Test data for Verify User Tests
  public final static String TEST_VERIFY_USER_1_EMAIL = "goodbye@gmail.com";
  public final static String TEST_VERIFY_USER_1_PASSWORD = "helllo";
  public final static int TEST_VERIFY_USER_1_ID = 17;
  public final static String TEST_VERIFY_USER_1_QUERY = String
      .format(
          "SELECT * FROM public.\"USERS\" WHERE \"EMAIL\"='%s';",
          TEST_VERIFY_USER_1_EMAIL);

  // Test data for Add User tests
  public final static String TEST_ADD_USER_1_EMAIL = "thisisdave@therock.co";
  public final static String TEST_ADD_USER_1_PASSWORD = "nicepasswordboyz";
  public final static String TEST_ADD_USER_1_FIRSTNAME = "Not";
  public final static String TEST_ADD_USER_1_LASTNAME = "Dave";
  public final static int TEST_ADD_USER_1_ID = 87;
  public final static String TEST_ADD_USER_1_QUERY = String
      .format(
          "INSERT INTO public.\"USERS\" VALUES(DEFAULT, '%s','%s','%s','%s', DEFAULT);",
          TEST_ADD_USER_1_EMAIL, TEST_ADD_USER_1_PASSWORD,
          TEST_ADD_USER_1_FIRSTNAME, TEST_ADD_USER_1_LASTNAME);

  // Test data for Add Session tests
  public final static String TEST_ADD_SESSION_1_SID = "he234few3c2355cs23jcp0";
  public final static int TEST_ADD_SESSION_1_ID = 67;
  public final static String TEST_ADD_SESSION_1_QUERY = String.format(
      "INSERT INTO \"SESSIONS\" VALUES ('%s', %s, DEFAULT);",
      TEST_ADD_SESSION_1_SID, TEST_ADD_SESSION_1_ID);

  @Test
  public void doesVerifyUserObtainCorrectData() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeQuery(TEST_VERIFY_USER_1_QUERY)).thenReturn(rs);
      when(rs.next()).thenReturn(true).thenReturn(false);
      when(rs.getInt(ID_COLUMN)).thenReturn(TEST_VERIFY_USER_1_ID);
      when(rs.getString(PASSWORD_COLUMN)).thenReturn(
          TEST_VERIFY_USER_1_PASSWORD);
      when(rs.getString(EMAIL_COLUMN)).thenReturn(TEST_VERIFY_USER_1_EMAIL);
    } catch (SQLException e) {
      fail("Something failed: " + e.getMessage());
    }

    UserRequest uq = new UserRequest(TEST_VERIFY_USER_1_EMAIL,
        TEST_VERIFY_USER_1_PASSWORD);
    UserResponse ur;
    try {
      ur = db.verifyUser(uq);
    } catch (SQLException | UserNotFoundException | InconsistentDataException e) {
      fail("Something failed: " + e.getMessage());
      return;
    }
    assertEquals(TEST_VERIFY_USER_1_EMAIL, ur.getEmail());
    assertEquals(TEST_VERIFY_USER_1_PASSWORD, ur.getHashedPassword());
    assertEquals(TEST_VERIFY_USER_1_ID, ur.getUserId());
  }

  @Test(expected = UserNotFoundException.class)
  public void doesVerifyUserThrowUserNotFoundExceptionWhenNeeded()
      throws UserNotFoundException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeQuery(TEST_VERIFY_USER_1_QUERY)).thenReturn(rs);
      when(rs.next()).thenReturn(false);
    } catch (SQLException e) {
      fail("Something failed: " + e.getMessage());
    }

    UserRequest uq = new UserRequest(TEST_VERIFY_USER_1_EMAIL,
        TEST_VERIFY_USER_1_PASSWORD);
    try {
      db.verifyUser(uq);
    } catch (SQLException | InconsistentDataException e) {
      fail("Something failed: " + e.getMessage());
    }
    fail("UserNotFoundException should have been thrown");
  }

  @Test(expected = SQLException.class)
  public void doesVerifyUserThrowSQLExceptionWhenCreateStatementFails()
      throws SQLException {
    when(conn.createStatement()).thenThrow(new SQLException());

    UserRequest uq = new UserRequest(TEST_VERIFY_USER_1_EMAIL,
        TEST_VERIFY_USER_1_PASSWORD);
    try {
      db.verifyUser(uq);
    } catch (UserNotFoundException | InconsistentDataException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test(expected = InconsistentDataException.class)
  public void doesVerifyUserThrowIDExceptionWhenNeeded()
      throws InconsistentDataException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeQuery(TEST_VERIFY_USER_1_QUERY)).thenReturn(rs);
      when(rs.next()).thenReturn(true);
      when(rs.next()).thenReturn(true);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }

    UserRequest uq = new UserRequest(TEST_VERIFY_USER_1_EMAIL,
        TEST_VERIFY_USER_1_PASSWORD);
    try {
      db.verifyUser(uq);
    } catch (SQLException | UserNotFoundException e) {
      fail("Something failed: " + e.getMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void doesVerifyUserThrowSQLExceptionWhenQueryFails()
      throws SQLException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
    } catch (SQLException e) {
      fail("Something failed (Wrong throw): " + e.getMessage());
    }
    when(stmt.executeQuery(TEST_VERIFY_USER_1_QUERY)).thenThrow(
        new SQLException());

    UserRequest uq = new UserRequest(TEST_VERIFY_USER_1_EMAIL,
        TEST_VERIFY_USER_1_PASSWORD);
    try {
      db.verifyUser(uq);
    } catch (UserNotFoundException | InconsistentDataException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test
  public void doesAddUserCorrectlyApplyTheQueryToTheDatabase() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.getGeneratedKeys()).thenReturn(rs);
      when(rs.next()).thenReturn(true);
      when(rs.getInt(ID_COLUMN)).thenReturn(TEST_ADD_USER_1_ID);
    } catch (SQLException e) {
      fail("Unexpected exception: " + e.getMessage());
    }

    try {
      assertEquals(db.addUser(new RegisterRequest(TEST_ADD_USER_1_EMAIL,
          TEST_ADD_USER_1_PASSWORD, TEST_ADD_USER_1_FIRSTNAME,
          TEST_ADD_USER_1_LASTNAME)), TEST_ADD_USER_1_ID);
      verify(stmt, times(1)).executeUpdate(TEST_ADD_USER_1_QUERY,
          Statement.RETURN_GENERATED_KEYS);
    } catch (SQLException e) {
      fail("Unexpected exception: " + e.getMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void doesAddUserThrowSQLExceptionWhenCreateStatemenrFails()
      throws SQLException {
    when(conn.createStatement()).thenThrow(new SQLException());
    db.addUser(new RegisterRequest(TEST_ADD_USER_1_EMAIL,
        TEST_ADD_USER_1_PASSWORD, TEST_ADD_USER_1_FIRSTNAME,
        TEST_ADD_USER_1_LASTNAME));
  }

  @Test(expected = SQLException.class)
  public void doesAddUserThrowSQLExceptionWhenQueryFails() throws SQLException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.getGeneratedKeys()).thenReturn(rs);
      when(rs.next()).thenReturn(false);
    } catch (SQLException e) {
      fail("Unexpected exception: " + e.getMessage());
    }
    assertEquals(db.addUser(new RegisterRequest(TEST_ADD_USER_1_EMAIL,
        TEST_ADD_USER_1_PASSWORD, TEST_ADD_USER_1_FIRSTNAME,
        TEST_ADD_USER_1_LASTNAME)), TEST_ADD_USER_1_ID);
    verify(stmt, times(1)).executeUpdate(TEST_ADD_USER_1_QUERY,
        Statement.RETURN_GENERATED_KEYS);

  }

  @Test
  public void doesAddSessionCorrectlyApplyTheQueryToTheDatabase() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.execute(TEST_ADD_SESSION_1_QUERY)).thenReturn(true);
    } catch (SQLException e) {
      fail("Unexpected expection: " + e.getMessage());
    }
    try {
      assertEquals(db.addSession(new SessionRequest(TEST_ADD_SESSION_1_ID,
          TEST_ADD_SESSION_1_SID)), true);
      verify(stmt, times(1)).execute(TEST_ADD_SESSION_1_QUERY);
    } catch (SQLException e) {
      fail("Unexpected expection: " + e.getMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void doesAddSessionCorrectlyThrowSQLExceptionWhenCreateStatmentFails()
      throws SQLException {
    when(conn.createStatement()).thenThrow(new SQLException());
    db.addSession(new SessionRequest(TEST_ADD_SESSION_1_ID,
        TEST_ADD_SESSION_1_SID));
  }

  @Test(expected = SQLException.class)
  public void doesAddSessionCorrectlyThrowSQLExceptionWhenQueryFails()
      throws SQLException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
    } catch (SQLException e) {
      fail("Unexpected expection: " + e.getMessage());
    }
    when(stmt.execute(TEST_ADD_SESSION_1_QUERY)).thenThrow(new SQLException());
    db.addSession(new SessionRequest(TEST_ADD_SESSION_1_ID,
        TEST_ADD_SESSION_1_SID));
    verify(stmt, times(1)).execute(TEST_ADD_SESSION_1_QUERY);
  }
}
