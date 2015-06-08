package chat;

/**
 * Enumerates ChatMessage types for DynamicUpdate sent to the subscribers
 * 
 * @author nc1813
 * @author pc3813
 * 
 */
public enum MessageType {
  EVENT_UPDATE("update/event"), EVENT_DELETE("delete/event"),

  CALENDAR_UPDATE("update/calendar"), CALENDAR_DELETE("delete/calendar");

  private String type;

  private MessageType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
