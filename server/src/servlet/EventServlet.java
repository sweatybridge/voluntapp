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
    if (userId == 0) {
      // response.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
      return;
    }

    // Retrieve event id
    String eid = request.getPathInfo().substring(1);
    if (eid == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Request must follow REST convention."));
      return;
    }
    int eventId = Integer.parseInt(eid);

    Response resp;
    try {
      resp = db.getEventAttendees(eventId);
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
    EventRequest eventReq =
        gson.fromJson(request.getReader(), EventRequest.class);

    if (!eventReq.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "The supplied event data are invalid."));
      return;
    }
    
    /* Verify if the user is allowed to publish events in the specified 
     * calendar. */
    if (!checkAccessRights(eventReq.getCalendarId(), request)) {
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
    EventRequest eventReq =
        gson.fromJson(request.getReader(), EventRequest.class);

    if (eventReq == null || !eventReq.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "The updated event data are invalid."));
      return;
    }
    String eventId = eventReq.getEventId();
    
    /* Verify if the user is allowed to edit events in the specified 
     * calendar. */
    if (!checkAccessRights(db.getCalendarId(Integer.parseInt(eventId)), request)) {
      return;
    }
    
    if (eventId != null) {
      try {
        if (!db.updateEvent(Integer.parseInt(eventId), eventReq)) {
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

    String eventId = request.getPathInfo().substring(1);
    
    /* Verify if the user is allowed to edit events in the specified 
     * calendar. */
    if (!checkAccessRights(db.getCalendarId(Integer.parseInt(eventId)), request)) {
      return;
    }

    if (eventId != null) {
      try {
        db.deleteEvent(Integer.parseInt(eventId));
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
   * Function which verifies if the user has enough privileges to add/modify/delete
   * events from a particular calendar.
   * 
   * @param eventReq - event data supplied by the user
   * @param request  - Http servlet request sent by the user
   * @return Boolean value indicating if the user is allowed to edit calendar 
   *         events.
   */
  private boolean checkAccessRights(int calendarId, 
      HttpServletRequest request) {
    SessionResponse sessionResponse = (SessionResponse) request
        .getAttribute(SessionResponse.class.getSimpleName());
    
    AuthLevel level = db.authoriseUser(sessionResponse.getUserId(), 
        calendarId);
    
    if (level == AuthLevel.NONE || level == AuthLevel.BASIC) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "You are not allowed to edit events of this calendar. " +
          "Owner / Admin priviledges are required."));
      return false;
    }
    return true;
  }
}
