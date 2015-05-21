package resp;

public class SuccessResponse extends Response {

  private String message;

  public SuccessResponse() {
    // no-arg constructor for gson serialisation
  }

  public SuccessResponse(String message) {
    this.message = message;
  }
}
