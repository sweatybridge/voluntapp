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

import req.UserRequest;
import resp.UserResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

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
  public final static String PASSWORD_COLUMN = "PASSWORD";
  public final static String ID_COLUMN = "ID";

  // Test Query Strings
  public final static String TEST_VERIFY_USER_1_EMAIL = "goodbye@gmail.com";
  public final static String TEST_VERIFY_USER_1_PASSWORD = "helllo";
  public final static int TEST_VERIFY_USER_1_ID = 17;
  public final static String TEST_VERIFY_USER_1_QUERY = String
      .format(
          "SELECT \"ID\" , \"PASSWORD\" FROM public.\"USERS\" WHERE \"EMAIL\"='%s';",
          TEST_VERIFY_USER_1_EMAIL);

  @Test
  public void doesVerifyUserObtainCorrectData() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeQuery(TEST_VERIFY_USER_1_QUERY)).thenReturn(rs);
      when(rs.next()).thenReturn(true).thenReturn(false);
      when(rs.getInt(ID_COLUMN)).thenReturn(TEST_VERIFY_USER_1_ID);
      when(rs.getString(PASSWORD_COLUMN)).thenReturn(
          TEST_VERIFY_USER_1_PASSWORD);
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

}
