package db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import req.SessionRequest;
import resp.SessionResponse;
import exception.SessionNotFoundException;

public class SessionManagerTest {

  private SessionManager sm;

  @Mock
  private DBInterface db;

  @Mock
  private SessionResponse sr;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    sm = new SessionManager(db);
  }

  // Test data for Session tests
  public final static String TEST_SESSION_1_SID = "1234qwer5678fghj0987";
  public final static int TEST_SESSION_1_ID = 92382;

  public final static int TEST_SESSION_1_INVADE = 2323;

  @Test
  public void doesCheckSessionCorrectlyReturnInformationFromTheDatabase() {
    try {
      when(db.getSession(TEST_SESSION_1_SID)).thenReturn(sr);
      when(sr.getSessionId()).thenReturn(TEST_SESSION_1_SID);
      when(sr.getUserId()).thenReturn(TEST_SESSION_1_ID);
      assertEquals(true, sm.checkSession(TEST_SESSION_1_SID, TEST_SESSION_1_ID));
    } catch (SQLException | SessionNotFoundException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test
  public void doesCheckSessionCorrectlyRejectInvalidUserSessions() {
    try {
      when(db.getSession(TEST_SESSION_1_SID)).thenReturn(sr);
      when(sr.getSessionId()).thenReturn(TEST_SESSION_1_SID);
      when(sr.getUserId()).thenReturn(TEST_SESSION_1_INVADE);
      assertEquals(false,
          sm.checkSession(TEST_SESSION_1_SID, TEST_SESSION_1_ID));
    } catch (SQLException | SessionNotFoundException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void doesCheckSessionCorrectlyPropagatesSQLException()
      throws SQLException {
    try {
      when(db.getSession(TEST_SESSION_1_SID)).thenThrow(new SQLException());
      sm.checkSession(TEST_SESSION_1_SID, TEST_SESSION_1_ID);
    } catch (SessionNotFoundException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test
  public void doesCheckSessionCorrectlyReturnWhenTheSessionDoesNotExist() {
    try {
      when(db.getSession(TEST_SESSION_1_SID)).thenThrow(
          new SessionNotFoundException(any(String.class)));
      assertEquals(false,
          sm.checkSession(TEST_SESSION_1_SID, TEST_SESSION_1_ID));
    } catch (SQLException | SessionNotFoundException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test(expected = SQLException.class)
  public void doesStartSessionCorrectlyThrowSQLException() throws SQLException {
    when(db.putSession(any(SessionRequest.class))).thenReturn(false);
    sm.startSession(TEST_SESSION_1_ID);
  }

  @Test
  public void doesCloseSessionCorrectlyReturnIfDeleteIsSuccessful() {
    try {
      when(db.deleteSession(TEST_SESSION_1_SID)).thenReturn(true);
      assertEquals(true, sm.closeSession(TEST_SESSION_1_SID));
      verify(db, times(1)).deleteSession(TEST_SESSION_1_SID);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }

  @Test
  public void doesCloseSessionCorrectlyReturnIfDeleteThrowsSQLException() {
    try {
      when(db.deleteSession(TEST_SESSION_1_SID)).thenThrow(new SQLException());
      assertEquals(false, sm.closeSession(TEST_SESSION_1_SID));
      verify(db, times(1)).deleteSession(TEST_SESSION_1_SID);
    } catch (SQLException e) {
      fail("Unexpected Exception: " + e.getMessage());
    }
  }
}
