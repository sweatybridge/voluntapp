package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Test;

import exception.UserNotFoundException;

import req.UserRequest;
import resp.UserResponse;

public class DBInterfaceTest {

  DBInterface db;
  SessionManager sm; 
  
  public DBInterfaceTest() {
    Connection conn;
    try {
      conn = DriverManager.getConnection(DBInterface.DATABASE_NAME, DBInterface.DATABASE_USER,
          DBInterface.DATABASE_PASS);
    } catch (SQLException e) {
      System.exit(0);
      return;
    }
    db = new DBInterface(conn);
    sm = new SessionManager(db);
  }

  @Test
  public void test() throws SQLException, UserNotFoundException {
    UserResponse ur = db.verifyUser(new UserRequest("bob@thebuilder.com",
        "HELLAKEDWQ"));
    System.out.println(ur.getEmail() + "\n" + ur.getPassword() + "\n"
        + ur.getUserId());
  }

  @Test
  public void test1() throws SQLException {
    sm.closeSession("1");
  }
}
