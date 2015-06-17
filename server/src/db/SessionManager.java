package db;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;

import req.SessionRequest;
import resp.SessionResponse;
import exception.SessionNotFoundException;

/**
 * The class for managing (opening, checking and closing) sessions for user on
 * the site.
 * 
 * It used the database as a storage as to keep session information even after
 * server restarts. This also allows for greater scalability.
 * 
 * @author bs2113
 * 
 */
/**
 * @author bs2113
 * 
 */
public class SessionManager {
  
  private static int SESSION_ID_LENGTH = 26;
  
  private SessionIdGenerater sessionGenerater;
  private DBInterface db;

  /**
   * @param db
   *          The database with which the session manager will interact with
   */
  public SessionManager(DBInterface db) {
    this.db = db;
    this.sessionGenerater = new SessionIdGenerater();
  }

  /**
   * Used to generate secure IDs to used to manage user sessions.
   * 
   * @author bs2113
   * 
   */
  private final class SessionIdGenerater {
    private SecureRandom random = new SecureRandom();

    public String genSessionId() {
      String result = new BigInteger(130, random).toString(32);
      while (result.length() < SESSION_ID_LENGTH) {
        result = '0' + result;
      }
      return result;
    }
  }

  /**
   * Gets a new session ID that hasn't been used before (TODO).
   * 
   * @return A new unique session ID.
   */
  private String getNewSessionId() {
    String id;
    // We run this until we find one that it not active
    do {
      id = sessionGenerater.genSessionId();
    } while (isIdAlive(id));
    return id;
  }

  /**
   * Checks whether a session ID is already live. NOT IMPLEMENTED YET (TODO).
   * 
   * @param id
   *          The session to test.
   * @return Whether or not the session exists already.
   */
  private boolean isIdAlive(String id) {
    return false;
  }

  /**
   * Starts a user session. Generates the ID and adds it to the database with
   * the user's ID.
   * 
   * @param userId
   *          The user to create the session for.
   * @return The new session ID to send back to the user.
   * @throws SQLException
   *           Thrown when there is an error with the database interaction.
   */
  public String startSession(int userId) throws SQLException {

    // Create a session table entry for the new session
    String newId = getNewSessionId();

    // Create and return the correct Session object
    if (!db.putSession(new SessionRequest(userId, newId))) {
      throw new SQLException();
    }
    // Should never get here without throwing an exception
    return newId;
  }

  /**
   * Use to find out if a session token provided by the user is valid and
   * correct.
   * 
   * @param sessionId
   *          The session ID to test (normally user provided).
   * @param userId
   *          The userID the session should belong to.
   * @return Whether or not the session exists and is valid.
   * @throws SQLException
   *           Thrown when there is an error with the database interaction.
   */
  public boolean checkSession(String sessionId, int userId) throws SQLException {
    try {
      SessionResponse session = db.getSession(sessionId);
      return session.getUserId() == userId;
    } catch (SessionNotFoundException e) {
      return false;
    }
  }

  /**
   * Refreshes a user session
   * 
   * @param userId
   *          The user whose session to refresh
   * @return The new value of the session Id
   * @throws SQLException
   *           Thrown if there was an error in database interaction.
   */
  public String refreshSession(int userId) throws SQLException {
    String newId = getNewSessionId();
    if (db.updateSession(userId, newId)) {
      return newId;
    } else {
      return null;
    }
  }

  /**
   * Use to close a user session.
   * 
   * @param sessionId
   *          The ID of the session to close.
   * @return Whether or not the session was successfully closed.
   */
  public boolean closeSession(String sessionId) {
    try {
      return db.deleteSession(sessionId);
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
}
