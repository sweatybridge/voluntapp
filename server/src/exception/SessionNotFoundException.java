package exception;

public class SessionNotFoundException extends Exception {

  private static final long serialVersionUID = 1L;

  public SessionNotFoundException(String string) {
    super(string);
  }

}
