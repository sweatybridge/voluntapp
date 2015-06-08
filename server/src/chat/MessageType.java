package chat;

public enum MessageType {
  EVENT_UPDATE("update/event"),
  EVENT_DELETE("delete/event"),
  
  CALENDAR_DELETE("delete/calendar");
  
  private String type;
  
  private MessageType(String type) {
    this.type = type;
  }
  
  public String getType() {
    return type;
  }
}
