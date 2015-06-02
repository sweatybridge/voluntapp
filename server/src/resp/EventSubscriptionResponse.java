package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventSubscriptionResponse extends Response {

  public static String EID_COLUMN = "EID";
  public static String UID_COLUMN = "UID";

  private List<EventResponse> joinedEvents = new ArrayList<>();

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

  public EventSubscriptionResponse(int eventId, int userId) {
    this.userId = userId;
    this.eventId = eventId;
  }

  /**
   * Constructs a get response.
   * 
   * @param eventId
   */
  public EventSubscriptionResponse(int userId) {
    this.userId = userId;
  }

  @Override
  public String getSQLQuery() {
    return String.format("SELECT (\"%s\") FROM \"EVENT_USER\" WHERE \"%s\"=?;",
        EID_COLUMN, UID_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepare) throws SQLException {
    prepare.setInt(1, userId);
  }

  @Override
  public String getSQLInsert() {
    return "INSERT INTO \"EVENT_USER\" VALUES (?, ?, DEFAULT);";
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

  public List<Integer> getJoinedEventIds() throws SQLException {
    List<Integer> events = new ArrayList<>();
    while (rs.next()) {
      events.add(rs.getInt(EventSubscriptionResponse.EID_COLUMN));
    }
    return events;
  }

  public void addEvent(EventResponse event) {
    joinedEvents.add(event);
  }
}
