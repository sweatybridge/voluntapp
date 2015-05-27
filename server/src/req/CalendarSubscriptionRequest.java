package req;

public class CalendarSubscriptionRequest implements Request {
  
  private String joinCode;
  
  /**
   * Fields excluded from deserialisation.
   */
  private transient int userId;
  
  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public CalendarSubscriptionRequest() {}
  
  public CalendarSubscriptionRequest(int userId) {
    this.userId = userId;
  }
  
  @Override
  public boolean isValid() {
    // TODO Implement error checking.
    return true;
  }
  
  public String getJoinCode() {
    return joinCode;
  }
  
  public int getUserId() {
    return userId;
  }
  
  public void setUserId(int userId) {
    this.userId = userId;
  }

}
