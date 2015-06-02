package servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.EventSubscriptionRequest;
import resp.ErrorResponse;
import resp.Response;
import resp.SuccessResponse;
import utils.AuthLevel;
import utils.ServletUtils;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import db.DBInterface;
import exception.InconsistentDataException;
import exception.InvalidActionException;

public class EventSubscriptionServlet extends HttpServlet {

  private final Gson gson;
  private final DBInterface db;
  private static final long serialVersionUID = 1L;

  public EventSubscriptionServlet(Gson gson, DBInterface db) {
    this.gson = gson;
    this.db = db;
  }

  // TODO: THIS IS ALL UNTESTED

  /**
   * Given the ID of the user (retrieved from the attribute of the request) get
   * the IDs of the events to which the user subscribed.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    int userId = ServletUtils.getUserId(request);
    if (userId == 0) {
      return;
    }

    Response subResp;
    try {
      subResp = db.getEventSubscription(userId);
    } catch (SQLException e) {
      subResp =
          new ErrorResponse("Error while retirieving the calendar IDs "
              + "from the database." + e.getMessage());
    }

    request.setAttribute(Response.class.getSimpleName(), subResp);
  }

  /**
   * Given the ID of an event, register the user's subscription to the event.
   * 
   * @throws IOException
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // TODO: Handle exception, should the servlet methods throw them?
    int userId = ServletUtils.getUserId(request);
    if (userId == 0) {
      return;
    }
    EventSubscriptionRequest subReq =
        gson.fromJson(request.getReader(), EventSubscriptionRequest.class);
    subReq.setUserId(userId);

    Response subResp;
    try {
      subResp = db.putEventSubscription(subReq);
    } catch (SQLException e) {
      subResp =
          new ErrorResponse("Error while registering event subscription ");
      response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    } catch (InvalidActionException e) {
      subResp = new ErrorResponse("Tried to join a full event!");
      response.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    request.setAttribute(Response.class.getSimpleName(), subResp);
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {

  }

  /**
   * Given the ID of the event, delete user's subscription to the event.
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {    
    int userId = ServletUtils.getUserId(request);
    int userToDelete;
    
    /* Get and validate the event ID. */
    String eventId = request.getPathInfo().substring(1);
    if (eventId == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "No event ID was specified."));
      return;
    }
    int eventID = Integer.parseInt(eventId);    
    
    /* Check if the user is an admin of the calendar, if yes then serialise 
     * the submitted user IDs. 
     */
    if (db.authoriseUser(userId, db.getCalendarId(eventID)) == AuthLevel.ADMIN) {
      try {
        EventSubscriptionRequest req = gson.fromJson(request.getReader(), 
            EventSubscriptionRequest.class);
        /* If admin sent payload, delete specified user's subscription, otherwise 
         * delete admin's subscription.
         */
        userToDelete = (req != null) ? req.getUserId() : userId;
      } catch (JsonSyntaxException | JsonIOException | IOException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Could not serialize JSON object."));
        return;
      }
    } else {
      userToDelete = userId;
    }
    
    /* Prevent deleting subscription from past events. */    
    try {
      if (isPastEvent(eventID)) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "You cannot delete subscription to a past event."));
        return;
      }
    } catch (NumberFormatException | SQLException e1) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Data base error occured."));
    }
    
    /* Delete event subscription(s). */
    Response resp = null;
    try {
      if (db.deleteEventSubscription(eventID, userToDelete)) {
        resp = new SuccessResponse("Unsubscribed from event");
      } else {
        resp = new ErrorResponse("Unsubscribing failed, you are not subscribed");
      }
    } catch (SQLException e) {
      resp = new ErrorResponse("Unsubscribing went wrong, this is bad");
    } catch (InconsistentDataException e) {
      resp = new ErrorResponse(e.getMessage());
    }
    request.setAttribute(Response.class.getSimpleName(), resp);
  }
  
  
  private boolean isPastEvent(int eventId) throws SQLException {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    Date currentDate = calendar.getTime();
    Timestamp eventEndDate = db.getEventEndTime(eventId);
    return currentDate.getTime() > eventEndDate.getTime();
  }
}
