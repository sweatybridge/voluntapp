package db;


import java.math.BigInteger;
import java.sql.*;
import java.security.SecureRandom;


public class DBInterface {
	
	public static void main(String[] args) {
		DBInterface db = new DBInterface();
		try {
			int res = db.verifyUser("bob@thebuilder.com", "431749843f15ba87f3765b51ffe2810d");
			if (res == -1) {
				System.out.println("Incorrect Password");
			} else {
				System.out.println("User ID is: " + res);
			}
		} catch (UserNotFoundException e) {
			System.out.println("User not found");
		}

	}
	
	public final static String DATABASE_NAME = "jdbc:postgresql://db.doc.ic.ac.uk/";
	public final static String DATABASE_USER = "g1427134_u";
	private final String DATABASE_PASS = "TRLzYYiVbD";
	private Connection conn; 
	private SessionIdGenerater sessionGenerater;
	
	public DBInterface() {
		// Create connection object
		try {
			conn = DriverManager.getConnection(DATABASE_NAME, DATABASE_USER, DATABASE_PASS);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
			
		}
		// Create a SessionIdGenerater for getting new Sessions
		
	}
	
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
	
	/* TODO: Create a log for the database error */
	
	/* Check to make sure that the user exists and the password is correct 
	 * returns -1 when the password is wrong or the users id when it is correct or -2
	 * when a database error has occurred.
	 * Also throws a UserNotFoundException if the user was not in the database */

	private int verifyUser(String email, String password) throws UserNotFoundException {
		
		// Get the password in and put it in the pass variable
	  LoginQuery query = new LoginQuery(email);
	  query(query);
	  
		String pass = query.getPassword();
		int id = query.getID();
		
		/* Check to see if the password are a match */
		if (!pass.equals(password)) {
			return -1;
		} else {
			return id;
		}
	}
	
	/* Start a session and returns a Session object with the session information */
	
	public Session startSession(String email, String password) throws UserNotFoundException {
		// Check user is in the database, verify password
		int id = verifyUser(email, password);
		if (id <= 0) {
			return null;
		}

		// Create a session table entry for the new session
        String newId = getNewSessionId();
		
		// Create and return the correct Session object
		
		return null;
	}
	
	/* Insert data into the database. */
  public boolean insert(SQLInsert insertion) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			return false;
		}
		
		try {
			boolean rs = stmt.execute(insertion.getSQLInsert());
			System.out.println(stmt.getResultSet());
			
		} catch (SQLException e) {
			System.err.println("Error in executing SQL INSERT query: " + e.getMessage());
			return false;
		}	
		return true;
	}
	
	/* Execute SQL query. */
	public boolean query(SQLQuery query) {
	  Statement stmt;
	  try {
	    stmt = conn.createStatement();
	  } catch (SQLException e) {
	    System.err.println("Error creating statement: " + e.getMessage());
	    return false;
	  }
	  
	  try {
	    ResultSet result = stmt.executeQuery(query.getSQLQuery());
	    query.setResult(result);
	    return true;
	  } catch (SQLException e) {
	    System.err.println("Error in executing SQL SELECT query: " + 
	      e.getMessage());
	    return false;
	  }
	}
	
	/* Closes the database connection, if there is an error just return
   * false */
  public boolean destory() {
    try {
      conn.close();
    } catch (SQLException e) {
      return false;
    }
    return true;
  }
}
