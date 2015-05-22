package resp;

/**
 * Represents a standard format that all API responses should conform to.
 * 
 * NOTE: All responses are only required to implement setters as GSON performs
 * the serialisation of private fields automatically.
 */
public abstract class Response {

  /**
   * Default status code of a successful response. Could also be used to trigger
   * dynamic behaviour on the client based on different status code.
   */
  protected int statusCode = 0;

}
