package utils;

import javax.servlet.http.HttpServletRequest;

import db.DBInterface;

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
    if (sessionResponse.getUserId() == -1) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Error - no user ID supplied."));
      return -1;
    }
    return sessionResponse.getUserId();
  }

  /**
   * Check if the user has the required (or higher) user rights.
   * 
   * @param calendarId
   * @param userId
   * @param requiredLevel
   * @return
   */
  public static boolean checkAccessRights(int calendarId, int eventId, int userId,
      AuthLevel requiredLevel, DBInterface db) {
    if (calendarId == 0) {
      calendarId = db.getCalendarId(new CalendarEventIdQuery(eventId));
    }
    AuthLevel level = db.authoriseUser(userId, calendarId);
    return level.ordinal() >= requiredLevel.ordinal();
  }
}
