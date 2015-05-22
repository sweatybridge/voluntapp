package req;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * Deserialized JSON object of an API request to register new user.
 */
public class RegisterRequest implements Request {

  /**
   * Invalid register request object to replace null checks.
   */
  public static final RegisterRequest INVALID = new RegisterRequest();

  private String email;
  private String password;
  private String firstName;
  private String lastName;

  @Override
  public boolean isValid() {
    return (email != null && EmailValidator.getInstance().isValid(email))
        && (password != null && password.length() >= 6)
        && (firstName != null && StringUtils.isAlpha(firstName))
        && (lastName == null || lastName.isEmpty() || StringUtils
            .isAlpha(lastName));
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }
}
