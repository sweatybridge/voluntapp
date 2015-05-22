package db;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;

import req.SessionRequest;

/**
 * The class for managing (opening, checking and closing)
 * sessions for user on the site.
 * 
 * It used the database as a storage as to keep session information
 * even after server restarts. This also allows for greater scalability.
 * 
 * @author bs2113
 *
 */
/**
 * @author bs2113
 *
 */
public class SessionManager {
	
	private SessionIdGenerater sessionGenerater;
	private DBInterface db;
	
	/**
	 * @param db The database with which the session manager will interact with
	 */
	public SessionManager(DBInterface db) {
		this.db = db;
		this.sessionGenerater = new SessionIdGenerater();
	}

	/**
	 * Used to generate secure IDs to used to manage user
	 * sessions. 
	 * 
	 * @author bs2113
	 *
	 */
	private final class SessionIdGenerater {
		private SecureRandom random = new SecureRandom();
		
		public String genSessionId() {
			return new BigInteger(130, random).toString(32);
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
	 * Checks whether a session ID is already live.
	 * NOT IMPLEMENTED YET (TODO).
	 * 
	 * @param id The session to test.
	 * @return Whether or not the session exists already.
	 */
	private boolean isIdAlive(String id) {
		return false;
	}
	
	
	/**
	 * Starts a user session.
	 * Generates the ID and adds it to the database with the user's ID.
	 * 
	 * @param userId The user to create the session for.
	 * @return The new session ID to send back to the user.
	 * @throws SQLException Thrown when there is an error with the database interaction.
	 */
	public String startSession(int userId) throws SQLException {
		
	  // Create a session table entry for the new session
      String newId = getNewSessionId();
		
	  // Create and return the correct Session object
      if(db.addSession(new SessionRequest(userId, newId))) {
        return newId;
      }
      // Should never get here without throwing an exception
      return null;
	}
	
	/**
	 * Use to find out if a session token provided by the user is valid and correct.
	 * 
	 * @param sessionId The session ID to test (normally user provided).
	 * @param userId The userID the session should belong to.
	 * @return Whether or not the session exists and is valid.
	 * @throws SQLException Thrown when there is an error with the database interaction.
	 */
	public boolean checkSession(String sessionId, int userId) throws SQLException {
		return db.getUserIdFromSession(sessionId) == userId;
	}
	
	/**
	 * Use to close a user session.
	 * 
	 * @param sessionId The ID of the session to close.
	 * @return Whether or not the session was successfully closed.
	 * @throws SQLException Thrown when there is an error with the database interaction.
	 */
	public boolean closeSession(String sessionId) throws SQLException {
		return db.deleteSession(sessionId);
	}
	
}
