package db;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SessionManager {
	
	private SessionIdGenerater sessionGenerater;

	public final class SessionIdGenerater {
		private SecureRandom random = new SecureRandom();
		
		public String genSessionId() {
			return new BigInteger(130, random).toString(32);
		}
	}
	
	
	/* Generate a session ID for the user, it can't already be inside the database */
	
	private String getNewSessionId() {
		String id;
		// We run this until we find one that it not active
		// We m
		do {
			id = sessionGenerater.genSessionId();
		} while (isIdAlive(id));
		return id;
	}
	

	/* Need to fill in this once we have a query structure set up */
	private boolean isIdAlive(String id) {
		return false;
	}
	
	
	/* Start a session and returns a Session object with the session information */
	
	public int startSession(int userId) {
		
		// Create a session table entry for the new session
        String newId = getNewSessionId();
		return userId;
		
		// Create and return the correct Session object
        
	}
	
}
