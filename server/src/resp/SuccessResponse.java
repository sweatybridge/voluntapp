package resp;

/**
 * A simple response indicating that the request is successfully handled.
 */
public class SuccessResponse extends Response {

  /**
   * User friendly success message.
   */
  private String message;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public SuccessResponse() {}

  /**
   * Constructs a simple success response with a message.
   * 
   * @param message user friendly success message
   */
  public SuccessResponse(String message) {
    this.message = message;
    statusCode = 0;
  }
}
