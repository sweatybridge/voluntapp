package sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import exception.InconsistentDataException;
import exception.UserNotFoundException;

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
    return "SELECT \"ID\" , \"PASSWORD\" FROM public.\"USERS\" WHERE \"EMAIL\"=" + 
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
  
  public void checkValid() throws UserNotFoundException, InconsistentDataException, SQLException {
    if(found == false) {
      throw new UserNotFoundException("The users information could not be found");
    }
    if(queryResult.next() == true) {
      throw new InconsistentDataException("The database is dead HALP!");
    }
  }

  /* Return user's password. */
  public String getPassword() throws SQLException {
    return queryResult.getString(PASSWORD_COLUMN);
  }
  
  /* Return user's ID. */
  public Integer getID() throws SQLException {
	return queryResult.getInt(ID_COLUMN);
  }
  
  /* Standard error message returned on SQL exception. */
  private String getErrorMessage(SQLException e) {
    return "ERROR while retrieving the result of login SQL " +
        "query. " + e.getMessage();
  }

}
