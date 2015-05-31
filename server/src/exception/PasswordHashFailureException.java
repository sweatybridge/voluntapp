package exception;

public class PasswordHashFailureException extends Exception {

  private static final long serialVersionUID = 1L;

  public PasswordHashFailureException(String msg) {
    super(msg);
  }

}
