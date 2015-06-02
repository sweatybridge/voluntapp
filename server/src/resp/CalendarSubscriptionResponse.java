package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sql.SQLInsert;
import sql.SQLQuery;

public class CalendarSubscriptionResponse extends Response implements SQLQuery,
    SQLInsert {

  /* Columns of the USER_CALENDAR table. */
  public static final String UID_COLUMN = "UID";
  public static final String CID_COLUMN = "CID";
  public static final String ROLE_COLUMN = "ROLE";

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
    return String
        .format(
            "SELECT \"%s\" FROM \"USER_CALENDAR\" JOIN \"CALENDAR\" ON \"USER_CALENDAR\".\"%s\" = \"CALENDAR\".\"%s\" WHERE \"%s\"=? AND \"%s\" = true;",
            CID_COLUMN, CID_COLUMN, CalendarResponse.CID_COLUMN, UID_COLUMN,
            CalendarResponse.ACTIVE_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    prepared.setInt(1, userId);
  }

  public ResultSet getResultSet() {
    return rs;
  }

  @Override
  public void setResult(ResultSet result) {
    rs = result;
  }

  public List<CalendarResponse> getCalendars() {
    return calendars;
  }

  @Override
  public String getSQLInsert() {
    return String
        .format(
            "INSERT INTO \"USER_CALENDAR\"(\"%s\",\"%s\") SELECT %d, \"ID\" FROM \"CALENDAR\" WHERE \"%s\"=?;",
            UID_COLUMN, CID_COLUMN, userId, CalendarResponse.JOIN_CODE_COLUMN);
  }

  @Override
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {
    prepared.setString(1, escape(joinCode));
  }
}
