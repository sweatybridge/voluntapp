package db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DBInterfaceTest {
  @Test
  public void queryFunctionMakesQueryToTheDataBaseUsingTheSpecifiedObject() {
    LoginQuery query = new LoginQuery("Hello");
    DBInterface db = new DBInterface();
    db.query(query);
    assertEquals(query.getPassword(), "HELLO");
    assertTrue(query.getID() == 3);
  }
}
