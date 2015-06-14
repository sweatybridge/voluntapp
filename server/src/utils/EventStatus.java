package utils;

public enum EventStatus {
  DELETED("deleted"),
  DISAPPROVED("disapproved"), 
  PENDING("pending"),
  ACTIVE("active");
  
  public static String STATUS_ENUM_NAME = "STATUS";
  
  private String name;
  
  private EventStatus(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public static EventStatus translateToEnum(String name) {
    switch(name) {
      case "deleted": return DELETED;
      case "disapproved": return DISAPPROVED;
      case "pending": return PENDING;
      case "active": return ACTIVE;
    }
    assert(false) : "Should not reach this place!";
    return null;
  }
}
