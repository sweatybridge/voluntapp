package resp;

/**
 * A successful response to a login request.
 */
public class UserResponse extends Response {

  /**
   * Details returned to the client after successful login.
   */
  private String email;
  private transient String hashedPassword;
  private int userId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public UserResponse() {}

  /**
   * Construct a successful user response with the given email, hashed password
   * and userId.
   * 
   * @param email Email of the user response
   * @param hashedPassword Password found in the database
   * @param userId The ID of the user requests
   */
  public UserResponse(String email, String hashedPassword, int userId) {
    this.email = email;
    this.hashedPassword = hashedPassword;
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public String getHashedPassword() {
    return hashedPassword;
  }

  public int getUserId() {
    return userId;
  }
}
