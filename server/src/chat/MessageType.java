package chat;

/**
 * Enumerates ChatMessage types for DynamicUpdate sent to the subscribers
 * 
 * @author nc1813
 * @author pc3813
 * 
 */
public enum MessageType {
  USER_ONLINE("online/user"),
  USER_OFFLINE("offline/user"),
  
  EVENT_UPDATE("update/event"),
  EVENT_DELETE("delete/event"),
  EVENT_JOIN("join/event"),
  EVENT_UNJOIN("unjoin/event"),

  CALENDAR_JOIN("join/calendar"),
  CALENDAR_UNJOIN("unjoin/calendar"),
  CALENDAR_UPDATE("update/calendar"),
  CALENDAR_DELETE("delete/calendar");

  private String type;

  private MessageType(String type) {
    this.type = type;
  }

  /**
   * Returns ChatMessage type of the Enum.
   * 
   * @return String ChatMessage type
   */
  public String getType() {
    return type;
  }
}
