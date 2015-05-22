package db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.Test;

public class DBInterfaceTest {
  @Test
  public void queryFunctionMakesQueryToTheDataBaseUsingTheSpecifiedObject() {
    LoginQuery query = new LoginQuery("Hello");
    DBInterface db = new DBInterface();
    try {
      db.query(query);
      assertEquals(query.getPassword(), "HELLO");
      assertTrue(query.getID() == 3);
    } catch (Exception e) {
      fail("ERROR");
    }
  }
  
  @Test
  public void insertAddsDataToTheSpecifiedTable() {
    SessionInsert s = new SessionInsert("12345", 2);
    DBInterface db = new DBInterface();
    try {
      db.insert(s);
    } catch (SQLException e) {
      fail("ERROR");
    }
  }
  
  @Test
  public void sessionQueryQuerriesTheUserID() {
    DBInterface db = new DBInterface();
    SessionQuery s = new SessionQuery("123");
    Integer user = null;
    try {
      db.query(s);
      user = s.getUserID();
    } catch (Exception e) {
      fail("Should not have thrown an exception!");
    }
    assertTrue(user == 2);
  }

}
