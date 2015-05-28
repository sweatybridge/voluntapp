package resp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventSubscriptionResponse extends Response {

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
  public EventSubscriptionResponse() {
  }

  public EventSubscriptionResponse(int eventId) {
    this.eventId = eventId;
  }

  public EventSubscriptionResponse(int eventId, int userId) {
    this.eventId = eventId;
    this.userId = userId;
  }

  public void setAttenendees(List<UserResponse> attendees) {
    this.attendees = attendees;
  }

  public List<UserResponse> getAttenendees() {
    return attendees;
  }

  @Override
  public String getSQLQuery() {
    return String.format(
        "SELECT COUNT (*) AS \"TOTAL\" FROM \"EVENT_USER\" WHERE \"%s\"=%d;",
        EID_COLUMN, eventId);
  }

  @Override
  public String getSQLInsert() {
    return String.format("INSERT INTO \"EVENT_USER\" VALUES (%d,%d);", eventId,
        userId);
  }

  // TODO: WE REALLY NEED A DELETE ONE ???
  @Override
  public String getSQLUpdate() {
    return String.format(
        "DELETE FROM \"EVENT_USER\" WHERE \"EID\"=%d AND \"UID\"=%d;", eventId,
        userId);
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

  @Override
  public void setResult(ResultSet result) {
    // TODO: Do we need this?
    rs = result;
    try {
      if (rs.next()) {
        // attendees = rs.getInt("TOTAL");
      }
    } catch (SQLException e) {
      System.err.println("Error while querring the EVENT_USER table.");
    }
  }

  public static void main(String[] args) {
    EventSubscriptionResponse resp = new EventSubscriptionResponse(12);
    System.out.println(resp.getSQLQuery());
  }
}
