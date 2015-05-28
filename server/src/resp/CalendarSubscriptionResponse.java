package resp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sql.SQLInsert;
import sql.SQLQuery;

public class CalendarSubscriptionResponse extends Response implements SQLQuery,
    SQLInsert {

  /* Columns of the USER_CALENDAR table. */
  private static final String UID_COLUMN = "UID";
  private static final String CID_COLUMN = "CID";

  private List<CalendarResponse> calendars = new ArrayList<>();
  private String joinCode;
  /**
   * Fields excluded from serialisation.
   */
  private transient int userId;
  private transient ResultSet rs;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public CalendarSubscriptionResponse() {
  }

  public CalendarSubscriptionResponse(int userId) {
    this.userId = userId;
  }

  public CalendarSubscriptionResponse(int userId, String joinCode) {
    this.userId = userId;
    this.joinCode = joinCode;
  }

  public void setCalendars(List<CalendarResponse> calendars) {
    this.calendars = calendars;
  }

  @Override
  public String getSQLQuery() {
    return String.format(""
        + "SELECT \"%s\" FROM \"USER_CALENDAR\" WHERE \"%s\"=%d;", CID_COLUMN,
        UID_COLUMN, userId);
  }

  @Override
  public void setResult(ResultSet result) {
    rs = result;
    try {
      while (rs.next()) {
        // calendarIds.add(rs.getInt(CID_COLUMN));
      }
    } catch (SQLException e) {
      System.err
          .println("Error while reading the results of USER_ID - CALENDAR_ID query.");
    }
  }

  public List<CalendarResponse> getCalendars() {
    return calendars;
  }

  public String getSQLInsert() {
    return String.format(""
        + "INSERT INTO \"USER_CALENDAR\"(\"%s\",\"%s\") SELECT %d, \"ID\" "
        + "FROM \"CALENDAR\" WHERE \"%s\"='%s';", UID_COLUMN, CID_COLUMN,
        userId, CalendarResponse.JOIN_CODE_COLUMN,
        joinCode.replace("\'", "\'\'"));
  }
}
