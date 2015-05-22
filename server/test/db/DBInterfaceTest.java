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
  /*@Test
  public void queryFunctionMakesQueryToTheDataBaseUsingTheSpecifiedObject() {
    LoginQuery query = new LoginQuery("Hello");
    DBInterface db = new DBInterface();
    db.query(query);
    try {
      assertEquals(query.getPassword(), "HELLO");
      assertTrue(query.getID() == 3);
    } catch (UserNotFoundException e) {
    }
  }
  
  @Test
  public void insertAddsDataToTheSpecifiedTable() {
    SessionInsert s = new SessionInsert("123", 2);
    DBInterface db = new DBInterface();
    db.insert(s);
  }*/
	
	
  DBInterface db = new DBInterface();
  
  @Test
  public void test() throws SQLException, UserNotFoundException {
	UserResponse ur = db.verifyUser(new UserRequest("bob@thebuilder.com", "HELLAKEDWQ"));
	System.out.println(ur.getEmail() + "\n" + ur.getPassword() + "\n" + ur.getUserId());
  }
  
  @Test
  public void test1() throws SQLException {
	  DBInterface db = new DBInterface();
	  System.out.println(db.addSession(new SessionRequest(1, "Hellloo")));
  }

}
