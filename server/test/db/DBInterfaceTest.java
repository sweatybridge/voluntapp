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
  
  @Test
  public void test1() throws SQLException, UserNotFoundException {
	  System.out.println(sm.checkSession("12345", 7));
  }
  
  @Test
  public void test2() throws SQLException, UserNotFoundException, InconsistentDataException {
	  System.out.println(db.updateUserInfo(1, new RegisterRequest("User 2", null,null, "User 3")));
  }

}
