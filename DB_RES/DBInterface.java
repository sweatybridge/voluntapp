
import java.sql.*;


public class DBInterface {

	Connection conn; 
	
	
	public static void main(String[] args) {
		DBInterface inter = new DBInterface();
		inter.insertUser("Hello1", "HELLO1", "GOODBYE1", "Goodbye1");
		inter.destory();
	}
	
	public DBInterface() {
		// Create connection object
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://db.doc.ic.ac.uk/", "g1427134_u", "TRLzYYiVbD");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
			
		}
	}
	
	public boolean insertUser(String email, String password, String firstName, String lastName) {
		Statement stmt;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.err.println("Error createing statement: " + e.getMessage());
			return false;
		}
		
		try {
			boolean rs = stmt.execute("INSERT INTO public.\"USERS\" VALUES(DEFAULT, '" + email + "','" 
			               + password + "','" + firstName + "','" + lastName + "', DEFAULT);");
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

}
