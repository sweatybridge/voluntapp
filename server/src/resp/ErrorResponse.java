package resp;

public class ErrorResponse extends Response {

  private String message;

  public ErrorResponse() {
    // no-arg constructor for gson serialisation
    statusCode = 1;
  }

  public ErrorResponse(String message) {
    this.message = message;
    statusCode = 1;
  }
}
