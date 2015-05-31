package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventAdminResponse extends Response {

  public static String EID_COLUMN = "EID";
  public static String UID_COLUMN = "UID";

  private List<UserResponse> attendees;

  /**
   * Fields excluded from serialisation.
   */
  private transient int eventId;
  private transient int userId;
  private transient ResultSet rs;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public EventAdminResponse() {}

  public EventAdminResponse(int eventId) {
    this.eventId = eventId;
  }

  public EventAdminResponse(int eventId, int userId) {
    this.eventId = eventId;
    this.userId = userId;
  }

  public void setAttendees(List<UserResponse> attendees) {
    this.attendees = attendees;
  }

  public List<UserResponse> getAttendees() {
    return attendees;
  }

  @Override
  public String getSQLQuery() {
    return String.format(
        "SELECT COUNT (*) AS \"TOTAL\" FROM \"EVENT_USER\" WHERE \"%s\"=?;",
        EID_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepare) throws SQLException {
    prepare.setInt(1, eventId);
  }

  /*
   * @Override public String getSQLInsert() { return
   * String.format("INSERT INTO \"EVENT_USER\" VALUES (%d,%d);", eventId,
   * userId); }
   */

  @Override
  public String getSQLInsert() {
    return "INSERT INTO \"EVENT_USER\" VALUES (?, ?);";
  }

  @Override
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {
    prepared.setInt(1, eventId);
    prepared.setInt(2, userId);
  }

  @Override
  public String getSQLDelete() {
    return String.format(
        "DELETE FROM \"EVENT_USER\" WHERE \"%s\"=? AND \"%s\"=?;", EID_COLUMN,
        UID_COLUMN);
  }

  @Override
  public void formatSQLDelete(PreparedStatement prepared) throws SQLException {
    int i = 1;
    prepared.setInt(i++, eventId);
    prepared.setInt(i++, userId);
  }

  public String getSQLUserCount() {
    return String.format(
        "SELECT (\"UID\") FROM \"EVENT_USER\" WHERE \"EID\" = %d;", eventId);
  }

  public List<Integer> getSubscriberList() throws SQLException {
    // Untested
    List<Integer> users = new ArrayList<>();
    while (rs.next()) {
      users.add(rs.getInt(UID_COLUMN));
    }
    return users;
  }

  public ResultSet getResultSet() {
    return rs;
  }

  @Override
  public void setResult(ResultSet result) {
    rs = result;
  }
}
