package req;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import exception.PasswordHashFailureException;

import utils.PasswordUtils;

/**
 * Deserialized JSON object of an API request to register new user.
 */
public class RegisterRequest implements Request {

  /**
   * Registration details sent by the client.
   */
  private String email;
  private String password;
  private String currentPassword; // Used for updating user
  private String firstName;
  private String lastName;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public RegisterRequest() {
  }

  public RegisterRequest(String email, String password, String firstName,
      String lastName) {
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
  }

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
  
  public void hashPassword() throws PasswordHashFailureException {
	  password = PasswordUtils.getPasswordHash(password);
  }

  public String getPassword() {
    return password;
  }
  
  public String getCurrentPassword() {
	  return currentPassword;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }
}
