package resp;

import java.sql.ResultSet;

public class EventSubscriptionResponse extends Response {
  
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

}
