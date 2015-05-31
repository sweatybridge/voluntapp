package exception;

public class InconsistentDataException extends Exception {
	
  private static final long serialVersionUID = 1L;

  public InconsistentDataException(String message) {
		super(message);
	}

}
