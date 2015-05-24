package exception;

public class SessionNotFoundException extends Exception {

  public SessionNotFoundException(String string) {
    super(string);
  }

  public SessionNotFoundException() {
    super();
  }

  private static final long serialVersionUID = 1L;

}
