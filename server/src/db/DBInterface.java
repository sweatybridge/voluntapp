package db;


import java.sql.*;


public class DBInterface {

	Connection conn; 
	
	public DBInterface() {
		// Create connection object
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://db.doc.ic.ac.uk/", "g1427134_u", "TRLzYYiVbD");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
			
		}
	}
	
	public boolean insert(SQLInsert insertion) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.err.println("Error createing statement: " + e.getMessage());
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
	
	public boolean destory() {
		try {
			conn.close();
		} catch (SQLException e) {
			System.err.println("Error closeing the connection: " + e.getMessage());
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
}
