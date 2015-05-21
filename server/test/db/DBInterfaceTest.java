package db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class DBInterfaceTest {
  @Test
  public void queryFunctionMakesQueryToTheDataBaseUsingTheSpecifiedObject() {
    LoginQuery query = new LoginQuery("Hello");
    DBInterface db = new DBInterface();
    db.query(query);
    try {
      assertEquals(query.getPassword(), "HELLO");
      assertTrue(query.getID() == 3);
    } catch (UserNotFoundException e) {
      fail("Not yet implemented");
    }
  }
}
