package chat;

public enum MessageType {
  EVENT_UPDATE("update/event");
  
  private String type;
  
  private MessageType(String type) {
    this.type = type;
  }
  
  public String getType() {
    return type;
  }
}
