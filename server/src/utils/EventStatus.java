package utils;

public enum EventStatus {
  DELETED("deleted"),
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
}
