package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.EventRequest;
import resp.ErrorResponse;
import resp.EventResponse;
import resp.Response;
import resp.SuccessResponse;
import utils.AuthLevel;
import utils.CalendarEventIdQuery;
import utils.EventStatus;
import utils.ServletUtils;

import chat.DynamicUpdate;

import com.google.gson.Gson;

import db.DBInterface;
import exception.EventNotFoundException;
import exception.InconsistentDataException;
import exception.UserNotFoundException;

/**
 * End point for event related actions. Implements standard 4 methods for
 * fetching, creating, updating and deleting events. Checks user rights before
 * performing previliged actions.
 * 
 * @author nc1813
 * 
 */
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

    /*
     * Verify if the user is allowed to preview info about event attendees in
     * the specified calendar.
     */
    int eventID = Integer.parseInt(eventId);
    if (!ServletUtils
        .checkAccessRights(0, eventID, userId, AuthLevel.ADMIN, db)) {
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

    EventRequest eventReq = gson.fromJson(request.getReader(),
        EventRequest.class);

    if (eventReq == null || !eventReq.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "The supplied event data are invalid."));
      return;
    }

    try {
      /*
       * If user is an ADMIN, set event status to ACTIVE, if user is an editor,
       * set the event status to PENDING.
       */
      EventStatus status = EventStatus.DELETED;
      AuthLevel role = db.authoriseUser(userId, eventReq.getCalendarId());
      switch (role) {
      case ADMIN:
        status = EventStatus.ACTIVE;
        break;
      case EDITOR:
        status = EventStatus.PENDING;
        break;
      case BASIC:
      case NONE:
        setUnauthorisedAccessErrorResponse(request);
        return;
      }

      EventResponse resp = db.putEvent(eventReq, status, userId);
      resp.setCalendarId(eventReq.getCalendarId());
      /*
       * If user is an admin, send and update to all users that are online,
       * otherwise send the update to editors and admins only.
       */
      boolean all = (role == AuthLevel.ADMIN);
      DynamicUpdate.sendEventUpdate(eventReq.getCalendarId(), resp, all);
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

    // parse event id
    String eid = request.getPathInfo().substring(1);
    if (eid == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "The ID of the event to be deleted was not specified."));
      return;
    }
    int eventId = Integer.parseInt(eid);

    // parse partial updates
    EventRequest eventReq = gson.fromJson(request.getReader(),
        EventRequest.class);
    if (eventReq == null || !eventReq.isPartiallyValid()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "The updated event data are invalid."));
      return;
    }
    eventReq.setEventId(eventId);

    int calendarId = db.getCalendarId(new CalendarEventIdQuery(eventId));
    AuthLevel role = db.authoriseUser(userId, calendarId);
    EventStatus status = eventReq.getStatus();

    /* Prevent users from deleting events by doing updates. */
    if (status == EventStatus.DELETED) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Request not RESTful enough, use DELETE method to delete events."));
      return;
    }

    /* Only editors and admins can update the events. */
    if ((role != AuthLevel.EDITOR && role != AuthLevel.ADMIN)
        ||
        /* Only admins are allowed to activate or disapprove events. */
        ((status == EventStatus.ACTIVE || status == EventStatus.DISAPPROVED) && role != AuthLevel.ADMIN)) {
      setUnauthorisedAccessErrorResponse(request);
      return;
    }

    /* Editor's update sets the status of the event to pending. */
    EventStatus eventStatus = null;
    try {
      eventStatus = db.getEvent(eventId, role).getStatus();
    } catch (SQLException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Database error occurred while processing the request."));
      return;
    }

    /* If the event is active, don't reset its status to pending. */
    if (role == AuthLevel.EDITOR && eventStatus != EventStatus.ACTIVE) {
      status = EventStatus.PENDING;
      eventReq.setEventStatus(status);
    }
    eventReq.setCalendarId(calendarId);

    /* Try to update the event. */
    try {
      EventResponse resp = db.updateEvent(eventId, userId, eventReq);
      if (resp == null) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Update of the event data was not successful."));
      } else {
        /*
         * If the event is active then update everyone on the calendar.
         * Otherwise, update only editors and admins.
         */
        boolean updateEveryone = (resp.getStatus() == EventStatus.ACTIVE);
        DynamicUpdate.sendEventUpdate(calendarId, resp, updateEveryone);
        request.setAttribute(Response.class.getSimpleName(), resp);
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
    String eventIdString = request.getPathInfo().substring(1);

    if (eventIdString == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "No event ID was specified."));
      return;
    }

    /*
     * Verify if the user is allowed to delete events from the specified
     * calendar - is an editor.
     */
    int eventId = Integer.parseInt(eventIdString);
    int calendarId = db.getCalendarId(new CalendarEventIdQuery(eventId));
    AuthLevel level = db.authoriseUser(userId, calendarId);
    if (level.ordinal() < AuthLevel.EDITOR.ordinal()) {
      setUnauthorisedAccessErrorResponse(request);
      return;
    }

    /* Delete the event. */
    try {
      EventResponse resp = db.deleteEvent(eventId);
      resp.setCalendarId(calendarId);
      // Send dynamic update to the online subscribers
      DynamicUpdate.sendEventDelete(calendarId, resp);
      request.setAttribute(Response.class.getSimpleName(), new SuccessResponse(
          "The event was successfully deleted."));
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
