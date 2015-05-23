package req;

import org.apache.commons.validator.routines.EmailValidator;

/**
 * Deserialized JSON object of an API request to login a user.
 */
public class UserRequest implements Request {

  /**
   * Invalid login request object to replace null checks.
   */
  public static final UserRequest INVALID = new UserRequest();

  /**
   * Fields excluded from serialisation and deserialisation.
   */
  private transient int userId = -1;

  /**
   * Login details sent by the client.
   */
  private String email;
  private String password;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public UserRequest() {}

  /**
   * Constructor used to make internal database queries.
   * 
   * @param userId
   */
  public UserRequest(int userId) {
    this.userId = userId;
  }

  /**
   * Constructor used for tests.
   * 
   * @param email
   * @param password
   */
  public UserRequest(String email, String password) {
    this.email = email;
    this.password = password;
  }
  
  public UserRequest(int id, String email, String password) {
    this(email,password);
    this.userId = id;
  }

  @Override
  public boolean isValid() {
    return (email != null && EmailValidator.getInstance().isValid(email))
        && (password != null && password.length() >= 6);
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public int getUserId() {
    return userId;
  }
}
