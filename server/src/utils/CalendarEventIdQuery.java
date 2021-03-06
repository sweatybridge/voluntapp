package utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Query class that fetches the calendarId given an eventId from the database.
 * 
 * @author pc3813
 * 
 */
public class CalendarEventIdQuery implements CalendarIdQuery {

  private static final String CID_COLUMN = "CID";
  private static final String EID_COLUMN = "EID";
  private int eventId;
  private int calendarId;

  /**
   * Create the query for the given eventId.
   * 
   * @param eventId
   *          EventId for which the calendarId will be queried
   */
  public CalendarEventIdQuery(int eventId) {
    this.eventId = eventId;
  }

  @Override
  public String getSQLQuery() {
    return String.format(
        "SELECT \"%s\" FROM \"CALENDAR_EVENT\" WHERE \"%s\"=?;", CID_COLUMN,
        EID_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    prepared.setInt(1, eventId);
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

  @Override
  public int getCalendarId() {
    return calendarId;
  }
}
