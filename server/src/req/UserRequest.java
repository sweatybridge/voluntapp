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

  private String email;
  private String password;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public UserRequest() {}

  public UserRequest(String email, String password) {
    this.email = email;
    this.password = password;
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
}
