package utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CalendarJoinCodeIdQuery implements CalendarIdQuery {
  
  private static String CID_COLUMN = "ID";
  private static String JOIN_CODE_COLUMN = "JOIN_CODE";
  private String joinCode;
  private int cid;
  
  public CalendarJoinCodeIdQuery(String joinCode) {
    this.joinCode = joinCode;
  }
  
  @Override
  public String getSQLQuery() {
    return String.format("SELECT \"%s\" FROM \"CALENDAR\" WHERE \"%s\"=?;", 
        CID_COLUMN, JOIN_CODE_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    prepared.setString(1, joinCode);
  }

  @Override
  public void setResult(ResultSet result) {
    try {
      if (result.next()) {
        cid = result.getInt(CID_COLUMN);
      }
    } catch (SQLException e) {
      System.err.println("Data base error occured while retrieving the " +
      		"calndar ID given the join code.");
      e.printStackTrace();
    }
  }
  
  @Override
  public int getCalendarId() {
    return cid;
  }
  
}
