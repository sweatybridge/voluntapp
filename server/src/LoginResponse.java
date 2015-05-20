public class LoginResponse extends Response {

  private String sessionId;

  public LoginResponse() {
    // no-arg constructor for gson serialisation
  }

  public LoginResponse(String sessionId) {
    this.sessionId = sessionId;
  }
}
