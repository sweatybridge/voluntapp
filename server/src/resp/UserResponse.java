package resp;

/**
 * A successful response to a user request.
 */
public class UserResponse extends Response {

  /**
   * User details returned to the client.
   */
  private String email;
  private String firstName;
  private String lastName;
  private int userId;

  /**
   * Fields excluded from serialisation.
   */
  private transient String hashedPassword;

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
  public UserResponse(String email, String hashedPassword, int userId,
      String firstName, String lastName) {
    this.email = email;
    this.hashedPassword = hashedPassword;
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
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
