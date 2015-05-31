package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CalendarAuthResponse extends Response {

  private int userId;
  private int calendarId;
  private String accessPrivilege;

  public CalendarAuthResponse(int userId, int calendarId) {
    this.userId = userId;
    this.calendarId = calendarId;
  }

  /*
   * @Override public String getSQLQuery() { return String.format(
   * "SELECT \"%s\" FROM \"USER_CALENDAR\" WHERE \"%s\"=%d AND \"%s\"=%d",
   * CalendarSubscriptionResponse.ROLE_COLUMN,
   * CalendarSubscriptionResponse.UID_COLUMN, userId,
   * CalendarSubscriptionResponse.CID_COLUMN, calendarId); }
   */

  @Override
  public String getSQLQuery() {
    return String.format(
        "SELECT \"%s\" FROM \"USER_CALENDAR\" WHERE \"%s\"=? AND \"%s\"=?;",
        CalendarSubscriptionResponse.ROLE_COLUMN,
        CalendarSubscriptionResponse.UID_COLUMN,
        CalendarSubscriptionResponse.CID_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepare) throws SQLException {
    prepare.setInt(1, userId);
    prepare.setInt(2, calendarId);
  }

  @Override
  public void setResult(ResultSet result) {
    try {
      if (result.next()) {
        accessPrivilege = result
            .getString(CalendarSubscriptionResponse.ROLE_COLUMN);
      } else {
        accessPrivilege = "none";
      }
    } catch (SQLException e) {
      System.err
          .println("Database error occurred wile authorising user's access to a calendar / "
              + "user not registered to a calendar.");
    }
  }

  public String getAccessPrivilege() {
    return accessPrivilege;
  }
}
