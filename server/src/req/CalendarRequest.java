package req;


/**
 * Deserialized JSON object of an API request to create a calendar.
 */
public class CalendarRequest implements Request {

  /**
   * Invalid register request object to replace null checks.
   */
  public static final CalendarRequest INVALID = new CalendarRequest();

  /**
   * Calendar details sent by the client.
   */
  private String name;
  private boolean joinEnabled;

  /**
   * Fields excluded from deserialisation.
   */
  private transient String inviteCode;
  private transient int userId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public CalendarRequest() {}

  public CalendarRequest(String name, boolean joinEnabled, String inviteCode,
      int userId) {
    this.name = name;
    this.joinEnabled = joinEnabled;
    this.inviteCode = inviteCode;
    this.userId = userId;
  }
  
  @Override
  public boolean isValid() {
    return (name != null && !name.isEmpty());
  }
  
  public void setUserId(int userId) {
    this.userId = userId;
  }
  
  public void setInviteCode(String inviteCode) {
    this.inviteCode = inviteCode;
  }

  public String getName() {
    return name;
  }

  public boolean isJoinEnabled() {
    return joinEnabled;
  }

  public String getInviteCode() {
    return inviteCode;
  }

  public int getUserId() {
    return userId;
  }
}
