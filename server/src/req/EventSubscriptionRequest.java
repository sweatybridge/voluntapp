package req;

public class EventSubscriptionRequest implements Request {

  private int userId;
  private int eventId;
  /**
   * Fields excluded from deserialisation.
   */

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public EventSubscriptionRequest() {
  }
  
  public EventSubscriptionRequest(int userId, int eventId) {
    this.userId = userId;
    this.eventId = eventId;
  }

  @Override
  public boolean isValid() {
    /*
     * TODO: Check if: - the event is joinable - the user is allowed to join the
     * event (is registered to the corresponding calendar) - the capacity of the
     * event has not been exceeded
     */
    return true;
  }
  
  public int getUserId() {
    return userId;
  }

}
