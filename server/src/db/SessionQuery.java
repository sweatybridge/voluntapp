package db;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionQuery implements SQLQuery {
  
  private String sid;
  private ResultSet result;
  private static String USER_COLUMN = "USER";
  
  public SessionQuery(String sid) {
    this.sid = sid;
  }

  @Override
  public String getSQLQuery() {
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT \"USER\" FROM \"SESSIONS\" WHERE " +
    		"\"SID\"='").append(sid).append("';");
    return builder.toString();
  }

  @Override
  public void setResult(ResultSet result, int rowsAffected)
      throws UserNotFoundException {
    this.result = result;
    try {
      result.next();
    } catch (SQLException e) {
      System.err.println(getErrorMessage(e));
    }
  }
  
  public Integer getUserID() throws SQLException {
    return result.getInt(USER_COLUMN);
  }
  
  /* Standard error message returned on SQL exception. */
  private String getErrorMessage(SQLException e) {
    return "ERROR while retrieving the result of session SQL " +
        "query. " + e.getMessage();
  }
}
