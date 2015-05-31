package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventAggregateResponse extends Response {

  private List<UserResponse> attendees;

  /**
   * Fields excluded from serialisation.
   */
  private transient int eventId;
  private transient ResultSet rs;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public EventAggregateResponse() {}

  public EventAggregateResponse(int eventId) {
    this.eventId = eventId;
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
        "SELECT (\"%s\") FROM \"EVENT_USER\" WHERE \"%s\" = ?;",
        EventSubscriptionResponse.UID_COLUMN,
        EventSubscriptionResponse.EID_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepare) throws SQLException {
    prepare.setInt(1, eventId);
  }

  @Override
  public String getSQLInsert() {
    return null;
  }

  @Override
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {}

  @Override
  public String getSQLDelete() {
    return null;
  }

  @Override
  public void formatSQLDelete(PreparedStatement prepared) throws SQLException {}

  public List<Integer> getSubscriberList() throws SQLException {
    List<Integer> users = new ArrayList<>();
    while (rs.next()) {
      users.add(rs.getInt(EventSubscriptionResponse.UID_COLUMN));
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
