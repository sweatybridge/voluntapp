package exception;

public class InvalidActionException extends Exception {

  private static final long serialVersionUID = 1L;

  public InvalidActionException(String mess) {
    super(mess);
  }

}
