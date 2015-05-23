package resp;

/**
 * A successful response to a login request.
 */
public class LoginResponse extends Response {

  /**
   * Details returned to the client after successful login.
   */
  private String token;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public LoginResponse() {}

  /**
   * Construct a successful login response with auth token.
   * 
   * @param token authorization token
   */
  public LoginResponse(String token) {
    this.token = token;
  }
}
