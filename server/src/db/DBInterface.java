package db;


import java.sql.*;

import req.RegisterRequest;
import req.SessionRequest;
import req.UserRequest;
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
	
	
	/* Check to make sure that the user exists and the password is correct 
	 * returns -1 when the password is wrong or the users id when it is correct or -2
	 * when a database error has occurred.
	 * Also throws a UserNotFoundException if the user was not in the database */
	public UserResponse verifyUser(UserRequest uq) throws SQLException, UserNotFoundException {
		
		// Get the password in and put it in the pass variable
	    LoginQuery query = new LoginQuery(uq.getEmail());
	    query(query);

		return new UserResponse(uq.getEmail(), query.getPassword(), query.getID());	
	}
	
	public boolean addSession(SessionRequest sq) throws SQLException {
		SessionInsert si = new SessionInsert(sq.getSessionId(),sq.getUserId());
		return insert(si);
	}
	
	public boolean updateUserInfo(int userId, RegisterRequest rr) throws SQLException, UserNotFoundException, InconsistentDataException {
		UserUpdate uu = new UserUpdate(userId, rr.getEmail(), rr.getFirstName(), rr.getLastName(), rr.getPassword());
		int rows = update(uu);
		if (rows > 1) {
			throw new InconsistentDataException("Update user info modified more than 1 row!");
		} else if (rows == 0) {
			throw new UserNotFoundException("Update did not find any matching userId");
		}
		return update(uu) == 1;
	}
	
	public int getUserIdFromSession(String sid) throws SQLException, UserNotFoundException {
		SessionQuery sq = new SessionQuery(sid);
		query(sq);
		return sq.getUserID();
	}
	
	/* Insert data into the database. */
  private boolean insert(SQLInsert insertion) throws SQLException {
		Statement stmt;
		stmt = conn.createStatement();
		boolean rs = stmt.execute(insertion.getSQLInsert());
		return rs;
	}
	
	/* Executes an SQL Update */
	private int update(SQLInsert query) throws SQLException, UserNotFoundException {
		Statement stmt;
		stmt = conn.createStatement();
		int result = stmt.executeUpdate(query.getSQLInsert());
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
