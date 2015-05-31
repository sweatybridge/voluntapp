package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EventSubscriptionResponse extends Response {

  public static String EID_COLUMN = "EID";
  public static String UID_COLUMN = "UID";

  private List<EventResponse> pastEvents;

  /**
   * Fields excluded from serialisation.
   */
  private transient int eventId;
  private transient int userId;
  private transient ResultSet rs;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public EventSubscriptionResponse() {}

  public EventSubscriptionResponse(int userId, int eventId) {
    this.userId = userId;
    this.eventId = eventId;
  }

  public void setPastEvents(List<EventResponse> pastEvents) {
    this.pastEvents = pastEvents;
  }

  public List<EventResponse> getPastEvents() {
    return pastEvents;
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

  public ResultSet getResultSet() {
    return rs;
  }

  @Override
  public void setResult(ResultSet result) {
    rs = result;
  }
}
