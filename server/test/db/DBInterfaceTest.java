package db;

import java.sql.SQLException;

import org.junit.Test;

import req.RegisterRequest;
import req.UserRequest;
import resp.UserResponse;

public class DBInterfaceTest {

	
  DBInterface db = new DBInterface();
  SessionManager sm = new SessionManager(db);
  
  @Test
  public void test() throws SQLException, UserNotFoundException {
	UserResponse ur = db.verifyUser(new UserRequest("bob@thebuilder.com", "HELLAKEDWQ"));
	System.out.println(ur.getEmail() + "\n" + ur.getPassword() + "\n" + ur.getUserId());
  }
}
