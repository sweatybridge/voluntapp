package db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginQuery implements SQLQuery {
  
  private String email;
  private ResultSet queryResult;
  private static final String PASSWORD_COLUMN = "PASSWORD";
  private static final String ID_COLUMN = "ID";
  
  public LoginQuery(String email) {
    this.email = email;
    queryResult = null;
  }

  @Override
  public String getSQLQuery() {
    return "SELECT \"ID\" , \"PASSWORD\" FROM \"USERS\" WHERE \"EMAIL\" =" + 
      "'" + email + "'" + ";";
  }
  
  @Override
  public void setResult(ResultSet result) {
    this.queryResult = result;
    try {
      queryResult.next();
    } catch (SQLException e) {
      System.err.println(getErrorMessage(e));
    }
  }

  /* Return user's password. */
  public String getPassword() {
    String password = null;    
    try {
     password = queryResult.getString(PASSWORD_COLUMN);
     //assert (queryResult.next() == false);
    } catch (SQLException e) {
      System.err.println(getErrorMessage(e));
    }
    return password;
  }
  
  /* Return user's ID. */
  public Integer getID() {
    Integer ID = null;
    try {
      ID = queryResult.getInt(ID_COLUMN);
    } catch (SQLException e) {
      System.err.println(getErrorMessage(e));
    }
    return ID;
  }
  
  private String getErrorMessage(SQLException e) {
    return "ERROR while retrieving the result of login SQL " +
        "query. " + e.getMessage();
  }

}
