package req;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

public class RegisterRequest {

  private String email;
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  private String password;
  private String firstName;
  private String lastName;

  public boolean isValid() {
    return (email != null && EmailValidator.getInstance().isValid(email)) &&
        (password != null && password.length() >= 6) &&
        (firstName != null && StringUtils.isAlpha(firstName)) &&
        (lastName == null || StringUtils.isAlpha(lastName));
  }
}
