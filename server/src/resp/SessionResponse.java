package resp;

import java.sql.Timestamp;

/**
 * A successful response to a login request.
 */
public class SessionResponse extends Response {

  /**
   * Session details returned to the client.
   */
  private String sessionId;

  /**
   * Fields excluded from serialisation.
   */
  private transient Timestamp timeStamp;
  private transient int expiresIn;
  private transient int userId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public SessionResponse() {}

  /**
   * Construct a successful login response with auth token.
   * 
   * @param token authorization token
   */
  public SessionResponse(String sessionId) {
    this.sessionId = sessionId;
  }

  public SessionResponse(String sessionId, int userId) {
    this.sessionId = sessionId;
    this.userId = userId;
  }

  public int getUserId() {
    return userId;
  }

  public String getSessionId() {
    return sessionId;
  }
}
