package db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginQuery implements SQLQuery {
  
  private String email;
  private ResultSet queryResult;
  private static final String PASSWORD_COLUMN = "PASSWORD";
  private static final String ID_COLUMN = "ID";
  private boolean found = false;
  private final String userNotFoundMessage;
  
  public LoginQuery(String email) {
    this.email = email;
    queryResult = null;
    userNotFoundMessage = "User with email " + email + " was not found.";
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
      found = queryResult.next();
    } catch (SQLException e) {
      System.err.println(getErrorMessage(e));
    }
  }

  /* Return user's password. */
  public String getPassword() throws UserNotFoundException {
    /* If user not found in the data base, throw an exception. */
    if (found == false) {
      throw new UserNotFoundException(userNotFoundMessage);
    }
    /* Retrieve user's password. */
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
  public Integer getID() throws UserNotFoundException {
    /* If user not found in the data base, throw an exception. */
    if (found == false) {
      throw new UserNotFoundException(userNotFoundMessage);
    }
    /* Retrieve user's ID. */
    Integer ID = null;
    try {
      ID = queryResult.getInt(ID_COLUMN);
    } catch (SQLException e) {
      System.err.println(getErrorMessage(e));
    }
    return ID;
  }
  
  /* Standard error message returned on SQL exception. */
  private String getErrorMessage(SQLException e) {
    return "ERROR while retrieving the result of login SQL " +
        "query. " + e.getMessage();
  }

}
