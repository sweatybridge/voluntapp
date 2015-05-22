package db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.Test;

import req.SessionRequest;
import req.UserRequest;
import resp.UserResponse;

public class DBInterfaceTest {

	
  DBInterface db = new DBInterface();
  
  @Test
  public void test() throws SQLException, UserNotFoundException {
	UserResponse ur = db.verifyUser(new UserRequest("bob@thebuilder.com", "HELLAKEDWQ"));
	System.out.println(ur.getEmail() + "\n" + ur.getPassword() + "\n" + ur.getUserId());
  }
  
  @Test
  public void test1() throws SQLException {
	  DBInterface db = new DBInterface();
	  System.out.println(db.addSession(new SessionRequest(4, "")));
  }

}
