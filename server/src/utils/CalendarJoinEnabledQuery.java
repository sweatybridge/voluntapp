package utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import resp.CalendarResponse;
import sql.SQLQuery;

public class CalendarJoinEnabledQuery implements SQLQuery {
  
  private int cid;
  private boolean joinEnabled;
  
  public CalendarJoinEnabledQuery(int cid) {
    this.cid = cid;
  }
  
  @Override
  public String getSQLQuery() {
    return String.format("SELECT \"%s\" FROM \"CALENDAR\" WHERE \"%s\"=?;",
        CalendarResponse.JOIN_ENABLED_COLUMN, CalendarResponse.CID_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    prepared.setInt(1, cid);
  }

  @Override
  public void setResult(ResultSet result) {
    try {
      result.next();
      joinEnabled = result.getBoolean(CalendarResponse.JOIN_ENABLED_COLUMN);
    } catch (SQLException e) {
      System.err.println("Data base error occured while checking if calendar is joinable.");
    }
  }
  
  public boolean isJoinEnabled() {
    return joinEnabled;
  }
}
