package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;

public class EventAdminResponse extends Response {

  private List<UserResponse> attendees;

  /**
   * Fields excluded from serialisation.
   */
  private transient int eventId;
  private transient ResultSet rs;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public EventAdminResponse() {}

  public EventAdminResponse(int eventId) {
    this.eventId = eventId;
  }

  @VisibleForTesting
  public List<UserResponse> getAttendees() {
    return attendees;
  }

  public void setAttendees(List<UserResponse> attendees) {
    this.attendees = attendees;
  }

  @Override
  public String getSQLQuery() {
    return String.format(
        "SELECT (\"%s\") FROM \"EVENT_USER\" WHERE \"%s\" = ? AND (SELECT \"%s\"='active' FROM \"EVENT\" WHERE \"%s\"=?);",
        EventSubscriptionResponse.UID_COLUMN,
        EventSubscriptionResponse.EID_COLUMN,
        EventResponse.ACTIVE_COLUMN,
        EventResponse.EID_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepare) throws SQLException {
    prepare.setInt(1, eventId);
    prepare.setInt(2, eventId);
  }

  public List<Integer> getAttendeeIds() throws SQLException {
    List<Integer> users = new ArrayList<>();
    while (rs.next()) {
      users.add(rs.getInt(EventSubscriptionResponse.UID_COLUMN));
    }
    return users;
  }

  @Override
  public void setResult(ResultSet result) {
    rs = result;
  }
}
