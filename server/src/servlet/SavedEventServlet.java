package servlet;

import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import db.DBInterface;
import exception.InconsistentDataException;

import resp.ErrorResponse;
import resp.Response;
import resp.SuccessResponse;
import utils.AuthLevel;
import utils.CalendarEventIdQuery;
import utils.ServletUtils;

/**
 * End point for handling saved events for users. Implements the GET, POST,
 * DELETE methods for fetching, creating and deleting saved events. PUT method
 * is not supported for updating saved events. Checks user access rights before
 * each request.
 * 
 * @author nc1813
 * 
 */
public class SavedEventServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private final DBInterface db;

  public SavedEventServlet(DBInterface db) {
    this.db = db;
  }

  /**
   * Returns the list of EventResponse objects representing the events saved by
   * the user.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    int userId = ServletUtils.getUserId(request);

    try {
      request.setAttribute(Response.class.getSimpleName(),
          db.getSavedEvents(userId));
    } catch (SQLException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Data base error occured while retrieving user's saved events."));
    }
  }

  /**
   * Given the ID of the event, save it to the user's list of saved events.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    int userId = ServletUtils.getUserId(request);

    /* Check if the user provided event ID as a part of the URL. */
    String eventId = request.getPathInfo().substring(1);
    if (eventId == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "No valid event ID was provided."));
      return;
    }

    /*
     * Check if the user is allowed to access the event with the specified ID,
     * i.e. if the user is subscribed to the calendar with the specified event.
     */
    int eventID = Integer.parseInt(eventId);
    /* Get the ID of the calendar containing the event. */
    int calendarId = db.getCalendarId(new CalendarEventIdQuery(eventID));
    AuthLevel level = db.authoriseUser(userId, calendarId);
    if (level.ordinal() < AuthLevel.BASIC.ordinal()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "You do not have enough rights to access the given event."));
      return;
    }

    try {
      if (db.saveEvent(userId, eventID)) {
        request.setAttribute(Response.class.getSimpleName(),
            new SuccessResponse("Successfully saved the specified event."));
      } else {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Saving the specified event was unsuccessful."));
      }
    } catch (SQLException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Data base error occurred while saving the event."));
    } catch (InconsistentDataException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "More than one row was updated."));
    }
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
        "PUT method of the server is not supported."));
  }

  /**
   * Delete the event from user's list of saved events.
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    int userId = ServletUtils.getUserId(request);

    /* Check if the user provided event ID as a part of the URL. */
    String eventId = request.getPathInfo().substring(1);
    if (eventId == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "No valid event ID was provided."));
      return;
    }

    try {
      if (db.deleteSavedEvent(userId, Integer.parseInt(eventId))) {
        request
            .setAttribute(
                Response.class.getSimpleName(),
                new SuccessResponse(
                    "Successfully removed the event from the list of saved events."));
      } else {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Could not remove the event with the specified ID from the list "
                + "of saved events."));
      }
    } catch (SQLException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Data base error occurred while deleting the data from the data "
              + "base."));
    } catch (InconsistentDataException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Error, more than one row was deleted from the database."));
    }
  }
}
