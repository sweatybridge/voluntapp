package exception;

public class CalendarNotFoundException extends Exception {
  
  private static final long serialVersionUID = 1L;

  public CalendarNotFoundException(String message) {
    super(message);
  }
}
