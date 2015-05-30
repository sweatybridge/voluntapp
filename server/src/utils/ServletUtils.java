package utils;

import javax.servlet.http.HttpServletRequest;

import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;

public class ServletUtils {

  /**
   * Retrieve the authorization parameters from the request attribute. Generate
   * an error response when the user ID is invalid.
   * 
   * @param request
   *          sent to the server
   * @return ID of the user
   */
  public static int getUserId(HttpServletRequest request) {
    SessionResponse sessionResponse = (SessionResponse) request
        .getAttribute(SessionResponse.class.getSimpleName());

    /* No valid userId supplied - added for the sake of debugging. */
    if (sessionResponse.getUserId() == 0) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Error - no user ID supplied."));
      return 0;
    }
    return sessionResponse.getUserId();
  }
}
