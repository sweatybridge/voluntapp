package req;

/**
 * Represents a standard set of methods performed on all API request objects
 * deserialised from JSON.
 * 
 * NOTE: All request objects should only contain getters as they are immutable.
 */
public interface Request {

  /**
   * Validates all user supplied information in an API request before processing
   * for security reasons.
   * 
   * @return true if the deserialised object is valid
   */
  boolean isValid();

}
