package utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import resp.EventResponse;
import sql.SQLQuery;

public class EventEndTimeQuery implements SQLQuery {
  
  private final int eventId;
  private Timestamp endTime;
  private final static String resultName = "END_TIME";
  
  public EventEndTimeQuery(int eventId) {
    this.eventId = eventId;
  }

  @Override
  public String getSQLQuery() {
    return String.format(
        "SELECT \"%s\" + \"%s\" + \"%s\" AS \"%s\" FROM \"EVENT\" WHERE \"%s\" = ?;",
        EventResponse.DATE_COLUMN, EventResponse.TIME_COLUMN, 
        EventResponse.DURATION_COLUMN, resultName, EventResponse.EID_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    prepared.setInt(1, eventId);
  }

  @Override
  public void setResult(ResultSet result) {
    try {
      if (result.next()) {
        endTime = result.getTimestamp(resultName);
      }
    } catch (SQLException e) {
      System.err.println("Database error occured.");
      e.printStackTrace();
    }
  }
  
  public Timestamp getEndTime() {
    return endTime;
  }
}
