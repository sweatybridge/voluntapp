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

import req.EventRequest;
import req.RegisterRequest;
import req.SessionRequest;
import req.UserRequest;
import resp.EventResponse;
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
  public final static String TEST_GET_USER_1_EMAIL = "goodbye@gmail.com";
  public final static String TEST_GET_USER_1_PASSWORD = "helllo";
  public final static int TEST_GET_USER_1_ID = 17;
  public final static String TEST_GET_USER_1_QUERY = String.format(
      "SELECT * FROM public.\"USER\" WHERE \"EMAIL\"='%s';",
      TEST_GET_USER_1_EMAIL);
  public final static String TEST_GET_USER_2_EMAIL = "themoon@nasa.s";
  public final static String TEST_GET_USER_2_PASSWORD = "itsreallycheese";
  public final static int TEST_GET_USER_2_ID = 892;
  public final static String TEST_GET_USER_2_QUERY = String.format(
      "SELECT * FROM public.\"USER\" WHERE \"ID\"='%s';", TEST_GET_USER_2_ID);

  // Test data for Add User tests
  public final static String TEST_PUT_USER_1_EMAIL = "thisisdave@therock.co";
  public final static String TEST_PUT_USER_1_PASSWORD = "nicepasswordboyz";
  public final static String TEST_PUT_USER_1_FIRSTNAME = "Not";
  public final static String TEST_PUT_USER_1_LASTNAME = "Dave";
  public final static int TEST_PUT_USER_1_ID = 87;
  public final static String TEST_PUT_USER_1_QUERY = String
      .format(
          "INSERT INTO public.\"USER\" VALUES(DEFAULT, '%s','%s','%s','%s', DEFAULT);",
          TEST_PUT_USER_1_EMAIL, TEST_PUT_USER_1_PASSWORD,
          TEST_PUT_USER_1_FIRSTNAME, TEST_PUT_USER_1_LASTNAME);

  // Test data for Add Session tests
  public final static String TEST_PUT_SESSION_1_SID = "he234few3c2355cs23jcp0";
  public final static int TEST_PUT_SESSION_1_ID = 67;
  public final static String TEST_PUT_SESSION_1_QUERY = String.format(
      "INSERT INTO \"SESSION\" VALUES ('%s', %s, DEFAULT);",
      TEST_PUT_SESSION_1_SID, TEST_PUT_SESSION_1_ID);

  // Test data for Update User tests
  public final static int TEST_UPDATE_USER_1_ID = 2323;
  public final static String TEST_UPDATE_USER_1_EMAIL = "pppy@osuu.rip";
  public final static String TEST_UPDATE_USER_1_PASSWORD = "privatejet";
  public final static String TEST_UPDATE_USER_1_FIRSTNAME = "Dan";
  public final static String TEST_UPDATE_USER_1_LASTNAME = "Harold";
  public final static String TEST_UPDATE_USER_1_QUERY = String
      .format(
          "UPDATE public.\"USER\" SET \"EMAIL\"='%s',"
              + "\"FIRST_NAME\"='%s',\"LAST_NAME\"='%s',\"PASSWORD\"='%s' WHERE \"ID\"=%d",
          TEST_UPDATE_USER_1_EMAIL, TEST_UPDATE_USER_1_FIRSTNAME,
          TEST_UPDATE_USER_1_LASTNAME, TEST_UPDATE_USER_1_PASSWORD,
          TEST_UPDATE_USER_1_ID);

  // Test data for Put Event tests
  public final static String TEST_PUT_EVENT_1_TITLE = "My Awesome Event";
  public final static String TEST_PUT_EVENT_1_DESC = "The best event of all time, DONT MISS OUT!";
  public final static String TEST_PUT_EVENT_1_LOCATION = "Room 12, Riverdale, The Moon";
  public final static String TEST_PUT_EVENT_1_TIME = "10:20:04";
  public final static String TEST_PUT_EVENT_1_DATE = "04/06/1994";
  public final static String TEST_PUT_EVENT_1_DURATION = "00:10:20";
  public final static String TEST_PUT_EVENT_1_MAX = "100";
  public final static int TEST_PUT_EVENT_1_CALID = 4334;
  public final static int TEST_PUT_EVENT_1_EID = 82;
  public final static String TEST_PUT_EVENT_1_QUERY = String
      .format(
          "WITH x AS (INSERT INTO public.\"EVENT\" VALUES (DEFAULT, '%s', '%s', '%s', '%s', '%s', '%s', '{}', %s, true) RETURNING \"EID\") INSERT INTO public.\"CALENDAR_EVENT\" SELECT %d,\"EID\" FROM x;",
          TEST_PUT_EVENT_1_TITLE, TEST_PUT_EVENT_1_DESC,
          TEST_PUT_EVENT_1_LOCATION, TEST_PUT_EVENT_1_DATE,
          TEST_PUT_EVENT_1_TIME, TEST_PUT_EVENT_1_DURATION,
          TEST_PUT_EVENT_1_MAX, TEST_PUT_EVENT_1_CALID);

  // Test data for Delete Session Tests
  public final static String TEST_DELETE_SESSION_1_ID = "42094sdfsdf6456";
  public final static String TEST_DELETE_SESSION_1_QUERY = String.format(
      "DELETE FROM \"SESSION\" WHERE \"SID\"='%s';", TEST_DELETE_SESSION_1_ID);

  @Test
  public void doesGetUserObtainCorrectDataGivenEmail() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeQuery(TEST_GET_USER_1_QUERY)).thenReturn(rs);
      when(rs.next()).thenReturn(true).thenReturn(false);
      when(rs.getInt(ID_COLUMN)).thenReturn(TEST_GET_USER_1_ID);
      when(rs.getString(PASSWORD_COLUMN)).thenReturn(TEST_GET_USER_1_PASSWORD);
      when(rs.getString(EMAIL_COLUMN)).thenReturn(TEST_GET_USER_1_EMAIL);
    } catch (SQLException e) {
      fail("Something failed: " + e.getMessage());
    }

    UserRequest uq = new UserRequest(TEST_GET_USER_1_EMAIL,
        TEST_GET_USER_1_PASSWORD);
    UserResponse ur;
    try {
      ur = db.getUser(uq);
      verify(stmt, times(1)).executeQuery(TEST_GET_USER_1_QUERY);
    } catch (SQLException | UserNotFoundException | InconsistentDataException e) {
      fail("Something failed: " + e.getMessage());
      return;
    }
    assertEquals(TEST_GET_USER_1_EMAIL, ur.getEmail());
    assertEquals(TEST_GET_USER_1_PASSWORD, ur.getHashedPassword());
    assertEquals(TEST_GET_USER_1_ID, ur.getUserId());
  }

  @Test
  public void doesGetUserObtainCorrectDataGivenId() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeQuery(TEST_GET_USER_2_QUERY)).thenReturn(rs);
      when(rs.next()).thenReturn(true).thenReturn(false);
      when(rs.getInt(ID_COLUMN)).thenReturn(TEST_GET_USER_2_ID);
      when(rs.getString(PASSWORD_COLUMN)).thenReturn(TEST_GET_USER_2_PASSWORD);
      when(rs.getString(EMAIL_COLUMN)).thenReturn(TEST_GET_USER_2_EMAIL);
    } catch (SQLException e) {
      fail("Something failed: " + e.getMessage());
    }

    UserRequest uq = new UserRequest(TEST_GET_USER_2_ID, null,
        TEST_GET_USER_2_PASSWORD);
    UserResponse ur;
    try {
      ur = db.getUser(uq);
      verify(stmt, times(1)).executeQuery(TEST_GET_USER_2_QUERY);
    } catch (SQLException | UserNotFoundException | InconsistentDataException e) {
      fail("Something failed: " + e.getMessage());
      return;
    }
    assertEquals(TEST_GET_USER_2_EMAIL, ur.getEmail());
    assertEquals(TEST_GET_USER_2_PASSWORD, ur.getHashedPassword());
    assertEquals(TEST_GET_USER_2_ID, ur.getUserId());
  }

  @Test(expected = UserNotFoundException.class)
  public void doesGetUserThrowUserNotFoundExceptionWhenNeeded()
      throws UserNotFoundException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeQuery(TEST_GET_USER_1_QUERY)).thenReturn(rs);
      when(rs.next()).thenReturn(false);
    } catch (SQLException e) {
      fail("Something failed: " + e.getMessage());
    }

    UserRequest uq = new UserRequest(TEST_GET_USER_1_EMAIL,
        TEST_GET_USER_1_PASSWORD);
    try {
      db.getUser(uq);
      verify(stmt, times(1)).executeQuery(TEST_GET_USER_1_QUERY);
    } catch (SQLException | InconsistentDataException e) {
      fail("Something failed: " + e.getMessage());
    }
    fail("UserNotFoundException should have been thrown");
  }

  @Test(expected = SQLException.class)
  public void doesGetUserThrowSQLExceptionWhenCreateStatementFails()
      throws SQLException {
    when(conn.createStatement()).thenThrow(new SQLException());

    UserRequest uq = new UserRequest(TEST_GET_USER_1_EMAIL,
        TEST_GET_USER_1_PASSWORD);
    try {
      db.getUser(uq);
      verify(stmt, times(0)).executeQuery(any(String.class));
    } catch (UserNotFoundException | InconsistentDataException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test(expected = InconsistentDataException.class)
  public void doesGetUserThrowIDExceptionWhenNeeded()
      throws InconsistentDataException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeQuery(TEST_GET_USER_1_QUERY)).thenReturn(rs);
      when(rs.next()).thenReturn(true);
      when(rs.next()).thenReturn(true);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }

    UserRequest uq = new UserRequest(TEST_GET_USER_1_EMAIL,
        TEST_GET_USER_1_PASSWORD);
    try {
      db.getUser(uq);
      verify(stmt, times(1)).executeQuery(TEST_GET_USER_1_QUERY);
    } catch (SQLException | UserNotFoundException e) {
      fail("Something failed: " + e.getMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void doesGetUserThrowSQLExceptionWhenQueryFails() throws SQLException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
    } catch (SQLException e) {
      fail("Something failed (Wrong throw): " + e.getMessage());
    }
    when(stmt.executeQuery(TEST_GET_USER_1_QUERY))
        .thenThrow(new SQLException());

    UserRequest uq = new UserRequest(TEST_GET_USER_1_EMAIL,
        TEST_GET_USER_1_PASSWORD);
    try {
      db.getUser(uq);
      verify(stmt, times(1)).executeQuery(TEST_GET_USER_1_QUERY);
    } catch (UserNotFoundException | InconsistentDataException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test
  public void doesPutUserCorrectlyApplyTheQueryToTheDatabase() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.getGeneratedKeys()).thenReturn(rs);
      when(rs.next()).thenReturn(true);
      when(rs.getInt(ID_COLUMN)).thenReturn(TEST_PUT_USER_1_ID);
    } catch (SQLException e) {
      fail("Unexpected exception: " + e.getMessage());
    }

    try {
      assertEquals(db.putUser(new RegisterRequest(TEST_PUT_USER_1_EMAIL,
          TEST_PUT_USER_1_PASSWORD, TEST_PUT_USER_1_FIRSTNAME,
          TEST_PUT_USER_1_LASTNAME)), TEST_PUT_USER_1_ID);
      verify(stmt, times(1)).executeUpdate(TEST_PUT_USER_1_QUERY,
          Statement.RETURN_GENERATED_KEYS);
    } catch (SQLException e) {
      fail("Unexpected exception: " + e.getMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void doesPutUserThrowSQLExceptionWhenCreateStatementFails()
      throws SQLException {
    when(conn.createStatement()).thenThrow(new SQLException());
    db.putUser(new RegisterRequest(TEST_PUT_USER_1_EMAIL,
        TEST_PUT_USER_1_PASSWORD, TEST_PUT_USER_1_FIRSTNAME,
        TEST_PUT_USER_1_LASTNAME));
    verify(stmt, times(0)).executeUpdate(any(String.class), anyInt());
  }

  @Test(expected = SQLException.class)
  public void doesPutUserThrowSQLExceptionWhenQueryFails() throws SQLException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.getGeneratedKeys()).thenReturn(rs);
      when(rs.next()).thenReturn(false);
    } catch (SQLException e) {
      fail("Unexpected exception: " + e.getMessage());
    }
    assertEquals(db.putUser(new RegisterRequest(TEST_PUT_USER_1_EMAIL,
        TEST_PUT_USER_1_PASSWORD, TEST_PUT_USER_1_FIRSTNAME,
        TEST_PUT_USER_1_LASTNAME)), TEST_PUT_USER_1_ID);
    verify(stmt, times(1)).executeUpdate(TEST_PUT_USER_1_QUERY,
        Statement.RETURN_GENERATED_KEYS);

  }

  @Test
  public void doesPutSessionCorrectlyApplyTheQueryToTheDatabase() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeUpdate(TEST_PUT_SESSION_1_QUERY)).thenReturn(1);
    } catch (SQLException e) {
      fail("Unexpected expection: " + e.getMessage());
    }
    try {
      assertEquals(true, db.putSession(new SessionRequest(
          TEST_PUT_SESSION_1_ID, TEST_PUT_SESSION_1_SID)));
      verify(stmt, times(1)).executeUpdate(TEST_PUT_SESSION_1_QUERY);
    } catch (SQLException e) {
      fail("Unexpected expection: " + e.getMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void doesPutSessionCorrectlyThrowSQLExceptionWhenCreateStatmentFails()
      throws SQLException {
    when(conn.createStatement()).thenThrow(new SQLException());
    db.putSession(new SessionRequest(TEST_PUT_SESSION_1_ID,
        TEST_PUT_SESSION_1_SID));
    verify(stmt, times(0)).executeUpdate(any(String.class));
  }

  @Test(expected = SQLException.class)
  public void doesPutSessionCorrectlyThrowSQLExceptionWhenQueryFails()
      throws SQLException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
    } catch (SQLException e) {
      fail("Unexpected expection: " + e.getMessage());
    }
    when(stmt.executeUpdate(TEST_PUT_SESSION_1_QUERY)).thenThrow(new SQLException());
    db.putSession(new SessionRequest(TEST_PUT_SESSION_1_ID,
        TEST_PUT_SESSION_1_SID));
    verify(stmt, times(1)).executeUpdate(TEST_PUT_SESSION_1_QUERY);
  }

  @Test
  public void doesUpdateUserCorrectlyQueryTheDatabaseToChangeData() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeUpdate(TEST_UPDATE_USER_1_QUERY)).thenReturn(1);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
    RegisterRequest rr = new RegisterRequest(TEST_UPDATE_USER_1_EMAIL,
        TEST_UPDATE_USER_1_PASSWORD, TEST_UPDATE_USER_1_FIRSTNAME,
        TEST_UPDATE_USER_1_LASTNAME);
    try {
      assertEquals(db.updateUser(TEST_UPDATE_USER_1_ID, rr), true);
      verify(stmt, times(1)).executeUpdate(TEST_UPDATE_USER_1_QUERY);
    } catch (SQLException | InconsistentDataException | UserNotFoundException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test
  public void doesUpdateUserPreventDatabaseAccsessIfNothingToBeChanged() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
    RegisterRequest rr = new RegisterRequest(null, null, null, null);
    try {
      assertEquals(db.updateUser(TEST_UPDATE_USER_1_ID, rr), true);
      verify(stmt, times(0)).executeUpdate(any(String.class));
    } catch (SQLException | InconsistentDataException | UserNotFoundException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test(expected = InconsistentDataException.class)
  public void doesUpdateUserThrowIDExceptionWhenNeeded()
      throws InconsistentDataException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeUpdate(TEST_UPDATE_USER_1_QUERY)).thenReturn(7);
    } catch (SQLException e) {
      fail("Unexcepted Exception: " + e.getMessage());
    }
    RegisterRequest rr = new RegisterRequest(TEST_UPDATE_USER_1_EMAIL,
        TEST_UPDATE_USER_1_PASSWORD, TEST_UPDATE_USER_1_FIRSTNAME,
        TEST_UPDATE_USER_1_LASTNAME);
    try {
      db.updateUser(TEST_UPDATE_USER_1_ID, rr);
      verify(stmt, times(0)).executeUpdate(any(String.class));
    } catch (SQLException | UserNotFoundException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test(expected = UserNotFoundException.class)
  public void doesUpdateUserThrowUserNotFoundExceptionWhenNeeded()
      throws UserNotFoundException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeUpdate(TEST_UPDATE_USER_1_QUERY)).thenReturn(0);
    } catch (SQLException e) {
      fail("Unexcepted Exception: " + e.getMessage());
    }
    RegisterRequest rr = new RegisterRequest(TEST_UPDATE_USER_1_EMAIL,
        TEST_UPDATE_USER_1_PASSWORD, TEST_UPDATE_USER_1_FIRSTNAME,
        TEST_UPDATE_USER_1_LASTNAME);
    try {
      db.updateUser(TEST_UPDATE_USER_1_ID, rr);
      verify(stmt, times(0)).executeUpdate(any(String.class));
    } catch (SQLException | InconsistentDataException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void doesUpdateUserThrowSQLExceptionWhenConnectionFails()
      throws SQLException {
    when(conn.createStatement()).thenThrow(new SQLException());
    RegisterRequest rr = new RegisterRequest(TEST_UPDATE_USER_1_EMAIL,
        TEST_UPDATE_USER_1_PASSWORD, TEST_UPDATE_USER_1_FIRSTNAME,
        TEST_UPDATE_USER_1_LASTNAME);
    try {
      db.updateUser(TEST_UPDATE_USER_1_ID, rr);
    } catch (InconsistentDataException | UserNotFoundException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test
  public void doesDeleteSessionCorrectlyQueryTheDatabase() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeUpdate(TEST_DELETE_SESSION_1_QUERY)).thenReturn(1);
      assertEquals(db.deleteSession(TEST_DELETE_SESSION_1_ID), true);
      verify(stmt, times(1)).executeUpdate(TEST_DELETE_SESSION_1_QUERY);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void doesDeleteSessionCorrectlyThrowSQLExceptionWhenCreateStatmentFails()
      throws SQLException {
    when(conn.createStatement()).thenThrow(new SQLException());
    db.deleteSession(TEST_DELETE_SESSION_1_ID);
    verify(stmt, times(0)).execute(any(String.class));
  }

  @Test
  public void doesDeleteSessionFailWhenNoSessionsAreFound() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.executeUpdate(TEST_DELETE_SESSION_1_QUERY)).thenReturn(0);
      assertEquals(db.deleteSession(TEST_DELETE_SESSION_1_ID), false);
      verify(stmt, times(1)).executeUpdate(TEST_DELETE_SESSION_1_QUERY);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test
  public void doesPutEventCorrectlyCallTheInsertQuery() {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.getGeneratedKeys()).thenReturn(rs);
      when(rs.next()).thenReturn(true);
      when(rs.getInt(EventResponse.EID_COLUMN))
          .thenReturn(TEST_PUT_EVENT_1_EID);
      EventRequest er = new EventRequest(TEST_PUT_EVENT_1_TITLE,
          TEST_PUT_EVENT_1_DESC, TEST_PUT_EVENT_1_LOCATION,
          TEST_PUT_EVENT_1_DATE, TEST_PUT_EVENT_1_TIME,
          TEST_PUT_EVENT_1_DURATION, TEST_PUT_EVENT_1_MAX,
          TEST_PUT_EVENT_1_CALID);
      assertEquals(TEST_PUT_EVENT_1_EID, db.putEvent(er));
      verify(stmt, times(1)).executeUpdate(TEST_PUT_EVENT_1_QUERY,
          Statement.RETURN_GENERATED_KEYS);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void doesPutEventThrowSQLExceptionWhenCreateStatementFails()
      throws SQLException {
    when(conn.createStatement()).thenThrow(new SQLException());
    EventRequest er = new EventRequest(TEST_PUT_EVENT_1_TITLE,
        TEST_PUT_EVENT_1_DESC, TEST_PUT_EVENT_1_LOCATION,
        TEST_PUT_EVENT_1_DATE, TEST_PUT_EVENT_1_TIME,
        TEST_PUT_EVENT_1_DURATION, TEST_PUT_EVENT_1_MAX, TEST_PUT_EVENT_1_CALID);
    db.putEvent(er);
  }

  @Test(expected = SQLException.class)
  public void doesPutEventThrowSQLExceptionWhenQueryFails() throws SQLException {
    try {
      when(conn.createStatement()).thenReturn(stmt);
      when(stmt.getGeneratedKeys()).thenReturn(rs);
      when(rs.next()).thenReturn(false);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
    EventRequest er = new EventRequest(TEST_PUT_EVENT_1_TITLE,
        TEST_PUT_EVENT_1_DESC, TEST_PUT_EVENT_1_LOCATION,
        TEST_PUT_EVENT_1_DATE, TEST_PUT_EVENT_1_TIME,
        TEST_PUT_EVENT_1_DURATION, TEST_PUT_EVENT_1_MAX, TEST_PUT_EVENT_1_CALID);
    db.putEvent(er);
    verify(stmt, times(1)).executeUpdate(TEST_PUT_EVENT_1_QUERY,
        Statement.RETURN_GENERATED_KEYS);
  }

}
