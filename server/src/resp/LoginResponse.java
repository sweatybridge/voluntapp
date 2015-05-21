package resp;

/**
 * A successful response to a login request.
 */
public class LoginResponse extends Response {

  /**
   * Session ID returned to the client after successful login.
   */
  private String sessionId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public LoginResponse() {
    this(null);
  }

  /**
   * Construct a successful login response with the given session id.
   * 
   * @param sessionId session ID of the current login
   */
  public LoginResponse(String sessionId) {
    this.sessionId = sessionId;
  }
}
