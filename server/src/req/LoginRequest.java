package req;

import org.apache.commons.validator.routines.EmailValidator;

public class LoginRequest {

  private String email;
  private String password;

  public boolean isValid() {
    return (email != null && EmailValidator.getInstance().isValid(email))
        && (password != null && password.length() >= 6);
  }
}
