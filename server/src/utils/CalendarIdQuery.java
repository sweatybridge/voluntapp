package utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import sql.SQLQuery;

public class CalendarIdQuery implements SQLQuery {
  
  private static final String CID_COLUMN = "CID";
  private static final String EID_COLUMN = "EID";
  private int userId;
  private int calendarId;
  
  public CalendarIdQuery(int userId) {
    this.userId = userId;
  }
  
  @Override
  public String getSQLQuery() {
    return String.format("SELECT \"%s\" FROM \"CALENDAR_EVENT\" WHERE \"%s\"=?;", 
        CID_COLUMN, EID_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    prepared.setInt(1, userId);
  }

  @Override
  public void setResult(ResultSet result) {
    try {
      if (result.next()) {
        calendarId = result.getInt(CID_COLUMN);
      }
    } catch (SQLException e) {
      System.err.println("No event with the specified ID was found.");
      e.printStackTrace();
    }
  }
  
  public int getCalendarId() {
    return calendarId;
  }
}