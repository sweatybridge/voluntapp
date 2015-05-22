package resp;

/**
 * A successful response to a login request.
 */
public class UserResponse extends Response {

  /**
   * Details returned to the client after successful login.
   */
  private String email;
  private String password;
  private int userId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public UserResponse() {}

  /**
   * Construct a successful user response with the given email, password and userId.
   * 
   * @param email Email of the user response
   * @param password Password found in the database
   * @param userId The ID of the user requests
   */
  public UserResponse(String email, String password, int userId) {
    this.email = email;
    this.password = password;
    this.userId = userId;
  }
}
