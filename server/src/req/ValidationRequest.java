package req;

public class ValidationRequest implements Request {

  private String email;
  private String password;

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public boolean isValid() {
    return email != null && password != null;
  }

}
