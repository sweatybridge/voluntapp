package req;

public class EventSubscriptionRequest implements Request {

  private int userId;
  /**
   * Fields excluded from deserialisation.
   */

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public EventSubscriptionRequest() {
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
