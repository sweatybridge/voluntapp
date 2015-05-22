package db;


import java.math.BigInteger;
import java.sql.*;
import java.security.SecureRandom;

import req.UserRequest;
import req.Request;
import req.UserRequest;
import resp.UserResponse;
import resp.UserResponse;


public class DBInterface {
	
	public final static String DATABASE_NAME = "jdbc:postgresql://db.doc.ic.ac.uk/";
	public final static String DATABASE_USER = "g1427134_u";
	private final String DATABASE_PASS = "TRLzYYiVbD";
	private Connection conn; 
	
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
	
	
	/* TODO: Create a log for the database error */
	
	/* Check to make sure that the user exists and the password is correct 
	 * returns -1 when the password is wrong or the users id when it is correct or -2
	 * when a database error has occurred.
	 * Also throws a UserNotFoundException if the user was not in the database */

	private UserResponse verifyUser(UserRequest uq) throws SQLException, UserNotFoundException {
		
		// Get the password in and put it in the pass variable
	    LoginQuery query = new LoginQuery(uq.getEmail());
	    query(query);

		return new UserResponse(uq.getEmail(), query.getPassword(), query.getID());	
	}
	
	
	
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
	
	/* Executes an SQL Update */
	private int update(SQLQuery query) throws SQLException, UserNotFoundException {
		Statement stmt;
		stmt = conn.createStatement();
		int result = stmt.executeUpdate(query.getSQLQuery());
		query.setResult(null, result);
		return result;
	}
	
	/* Execute SQL query. */
	private boolean query(SQLQuery query) throws SQLException, UserNotFoundException {
	  Statement stmt;
	  stmt = conn.createStatement();
	  ResultSet result = stmt.executeQuery(query.getSQLQuery());
	  query.setResult(result, 0);
	  return true;
	}
}
