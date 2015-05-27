package resp;

import java.sql.ResultSet;
import java.sql.SQLException;


public class EventSubscriptionResponse extends Response {
  
  public static String EID_COLUMN = "EID";
  public static String UID_COLUMN = "UID";
  
  private int attendees;
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
  
  public EventSubscriptionResponse(int eventId) {
    this.eventId = eventId;
  }
  
  public EventSubscriptionResponse(int eventId, int userId) {
    this.eventId = eventId;
    this.userId = userId;
  }
  
  @Override
  public String getSQLQuery() {
    return String.format(
        "SELECT COUNT (*) AS \"TOTAL\" FROM \"EVENT_USER\" WHERE \"%s\"=%d;", 
        EID_COLUMN, eventId);
  }
  
  @Override
  public void setResult(ResultSet result) {
    rs = result;
    try {
      if (rs.next()) {
        attendees = rs.getInt("TOTAL");
      }
    } catch(SQLException e) {
      System.err.println("Error while querring the EVENT_USER table.");
    }
  }
  
  public static void main(String[] args) {
    EventSubscriptionResponse resp = new EventSubscriptionResponse(12);
    System.out.println(resp.getSQLQuery());
  }
}
