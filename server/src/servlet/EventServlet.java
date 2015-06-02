package servlet;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.EventRequest;
import resp.CalendarResponse;
import resp.ErrorResponse;
import resp.EventResponse;
import resp.EventSubscriptionResponse;
import resp.Response;
import resp.SessionResponse;
import resp.SuccessResponse;
import sql.SQLQuery;
import utils.AuthLevel;
import utils.ServletUtils;

import com.google.gson.Gson;

import db.DBInterface;
import exception.EventNotFoundException;
import exception.InconsistentDataException;
import exception.UserNotFoundException;

public class EventServlet extends HttpServlet {

  private final Gson gson;
  private final DBInterface db;
  private static final long serialVersionUID = 1L;

  public EventServlet(Gson gson, DBInterface db) {
    this.gson = gson;
    this.db = db;
  }

  /**
   * Given the ID of the event, return event details and all volunteer
   * information if the user is admin, otherwise unauthorized access.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    int userId = ServletUtils.getUserId(request);
    
    String eventId = request.getPathInfo().substring(1);
    if (eventId == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Request must follow REST convention."));
      return;
    }
        
    /* Verify if the user is allowed to preview info about event attendees 
     * in the specified calendar. */
    int eventID = Integer.parseInt(eventId);
    if (!checkAccessRights(eventID, userId, AuthLevel.ADMIN)) {
      setUnauthorisedAccessErrorResponse(request);
      return;
    }

    Response resp;
    try {
      resp = db.getEventAttendees(eventID);
    } catch (SQLException | UserNotFoundException | InconsistentDataException e) {
      resp = new ErrorResponse("Request must follow REST convention.");
    }
    request.setAttribute(Response.class.getSimpleName(), resp);
  }

  /**
   * Add the event to the database.
   * 
   * @throws IOException
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    int userId = ServletUtils.getUserId(request);
    
    EventRequest eventReq =
        gson.fromJson(request.getReader(), EventRequest.class);

    if (eventReq == null || !eventReq.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "The supplied event data are invalid."));
      return;
    }
    
    /* Verify if the user is allowed to publish events in the specified 
     * calendar. */
    if (!checkPOSTAccessRights(eventReq.getCalendarId(), userId, AuthLevel.EDITOR)) {
      setUnauthorisedAccessErrorResponse(request);
      return;
    }

    try {
      EventResponse resp = db.putEvent(eventReq);
      request.setAttribute(Response.class.getSimpleName(), resp);
    } catch (SQLException e) {
      e.printStackTrace();
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Error occurred while adding a new event to the database."));
    }
  }

  /**
   * Given the ID of the event, update the specified data of the event.
   * 
   * @throws IOException
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    int userId = ServletUtils.getUserId(request);
    
    EventRequest eventReq =
        gson.fromJson(request.getReader(), EventRequest.class);

    if (eventReq == null || !eventReq.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "The updated event data are invalid."));
      return;
    }
    String eventId = eventReq.getEventId();
    
    if (eventId != null) {
      /* Verify if the user is allowed to edit events in the specified 
       * calendar - is an editor. */
      int eventID = Integer.parseInt(eventId);
      if (!checkAccessRights(eventID, userId, AuthLevel.EDITOR)) {
        setUnauthorisedAccessErrorResponse(request);
        return;
      }
      /* Try to update the event. */
      try {
        if (!db.updateEvent(eventID, eventReq)) {
          request
              .setAttribute(Response.class.getSimpleName(), new ErrorResponse(
                  "Update of the event data was not successful."));
        } else {
          request.setAttribute(Response.class.getSimpleName(),
              new SuccessResponse("Event data were updated successfully."));
        }
      } catch (NumberFormatException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "One of the provided dates was incorrectly formatted."));
      } catch (SQLException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Error while saving the event update to the database."));
      } catch (EventNotFoundException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "No event with specified event ID exists in the database."));
      } catch (InconsistentDataException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Update left the database in an inconsistent state, "
                + "more than one row was updated."));
      }
    } else {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "No event ID was specified."));
    }
  }

  /**
   * Delete an event with the corresponding ID.
   * 
   * @throws IOException
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    int userId = ServletUtils.getUserId(request);
    String eventId = request.getPathInfo().substring(1);

    if (eventId != null) {
      /* Verify if the user is allowed to delete events from the specified 
       * calendar - is an editor. */
      int eventID = Integer.parseInt(eventId);
      if (!checkAccessRights(eventID, userId, AuthLevel.EDITOR)) {
        setUnauthorisedAccessErrorResponse(request);
        return;
      }
      /* Delete the event. */
      try {
        db.deleteEvent(eventID);
        request.setAttribute(Response.class.getSimpleName(),
            new SuccessResponse("The event was successfully deleted."));
      } catch (EventNotFoundException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "No event with specified event ID exists in the database."));
      } catch (SQLException e) {
        e.printStackTrace();
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Error while deleting the data from the database."));
      } catch (InconsistentDataException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Update left the database in an inconsistent state, "
                + "more than one row was deleted."));
      }
    } else {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "No event ID was specified."));
    }
  }
  
  /**
   * Check if the user has the required (or higher) user rights.
   *   
   * @param eventId
   * @param userId
   * @param requiredLevel
   * @return
   */
  private boolean checkAccessRights(int eventId, int userId, 
      AuthLevel requiredLevel) {
    AuthLevel level = db.authoriseUser(userId, db.getCalendarId(eventId));
    return level.ordinal() >= requiredLevel.ordinal();
  }
  
  /**
   * Check if the user has the required (or higher) user rights.
   * 
   * @param calendarId
   * @param userId
   * @param requiredLevel
   * @return
   */
  private boolean checkPOSTAccessRights(int calendarId, int userId, 
      AuthLevel requiredLevel) {
    AuthLevel level = db.authoriseUser(userId, calendarId);
    return level.ordinal() >= requiredLevel.ordinal();
  }
  
  /**
   * Set the error message indicating unauthorised access,
   * 
   * @param request
   */
  private void setUnauthorisedAccessErrorResponse(HttpServletRequest request) {
    request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
        "You do not have enough rights to perform the requested operation."));
  }
}
