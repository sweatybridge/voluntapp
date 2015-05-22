package req;

/**
 * Request object used for internal database queries.
 */
public class SessionRequest implements Request {

  private final int userId;
  private final String sessionId;

  public SessionRequest(int userId, String sessionId) {
    this.userId = userId;
    this.sessionId = sessionId;
  }

  public int getUserId() {
    return userId;
  }

  public String getSessionId() {
    return sessionId;
  }

  @Override
  public boolean isValid() {
    return true;
  }
}
