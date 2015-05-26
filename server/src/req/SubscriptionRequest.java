package req;

public class SubscriptionRequest implements Request {
  
  private String joinCode;
  
  /**
   * Fields excluded from deserialisation.
   */
  private transient int userId;
  
  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public SubscriptionRequest() {}
  
  public SubscriptionRequest(int userId) {
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
