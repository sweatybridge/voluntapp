package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.CalendarSubscriptionRequest;
import resp.ErrorResponse;
import resp.Response;
import resp.SuccessResponse;
import resp.UserResponse;
import utils.AuthLevel;
import utils.ServletUtils;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import db.DBInterface;
import exception.CalendarSubscriptionNotFoundException;
import exception.InconsistentDataException;
import exception.UserNotFoundException;

@WebServlet
public class CalendarSubscriptionServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final DBInterface db;
  private final Gson gson;

  public CalendarSubscriptionServlet(Gson gson, DBInterface db) {
    this.db = db;
    this.gson = gson;
  }

  /**
   * Given the ID of the user (retrieved from the attribute of the request) get
   * the IDs of the calendars to which the user subscribed.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    // Get the user id of the session
    int userId = ServletUtils.getUserId(request);
    Response subResp;

    // Check id, return error if not valid
    if (userId < 0) {
      subResp = new ErrorResponse("Invalid session user ID.");
      request.setAttribute(Response.class.getSimpleName(), subResp);
      return;
    }

    // Get the data from the database
    try {
      subResp = db.getUsersCalendars(userId);
    } catch (SQLException | InconsistentDataException e) {
      subResp = new ErrorResponse("Error while retirieving the calendar IDs "
          + "from the database." + e.getMessage());
    }
    request.setAttribute(Response.class.getSimpleName(), subResp);
  }

  /**
   * Given the ID of the user and the join code of the calendar, register user's
   * subscription to the specified calendar.
   * 
   * @throws IOException
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Get user id from session and make sure it is valid
    int userId = ServletUtils.getUserId(request);
    Response subResp;
    if (userId < 0) {
      subResp = new ErrorResponse("Invalid session user ID.");
      request.setAttribute(Response.class.getSimpleName(), subResp);
      return;
    }

    CalendarSubscriptionRequest subReq = gson.fromJson(request.getReader(),
        CalendarSubscriptionRequest.class);
    subReq.setUserId(userId);

    // Update database
    try {
      subResp = db.putCalendarSubscription(subReq);
    } catch (SQLException e) {
      subResp = new ErrorResponse("Error while registering user's calendar "
          + "subscription.");
    }
    request.setAttribute(Response.class.getSimpleName(), subResp);
  }
  
  /** 
   * Given a calendar id in the form /api/subscription/calendar/[id], if the current user has admin rights
   * updates the given userid (in the payload) to the specified auth level.
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    // Get and check the calendar id from the url
    if (request.getPathInfo() == null || request.getPathInfo().substring(1).length() < 1) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "No calendar ID specified."));
      return;
    }
    String calendarIdS = request.getPathInfo().substring(1);
    
    // Check if the current user has owner or admin rights
    int calendarId = Integer.parseInt(calendarIdS);
    assert(calendarId >= 0);
    int currentUserId = ServletUtils.getUserId(request);
    AuthLevel level = db.authoriseUser(currentUserId, calendarId);
    if (level != AuthLevel.ADMIN) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Insufficient rights to change user role."));
      return;
    }
    
    // Get the payload data, we are looking for userId (targetUserId) and role
    CalendarSubscriptionRequest req;
    try {
      req = gson.fromJson(request.getReader(), CalendarSubscriptionRequest.class);
    } catch (JsonSyntaxException | JsonIOException | IOException e) {
      e.printStackTrace();
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Invalid update payload."));
      return;
    }
    
    assert(req != null);
    // Get the target user id from email
    UserResponse targetUser;
    try {
      targetUser = db.getUser(req.getTargetUserEmail());
    } catch (SQLException e1) {
      e1.printStackTrace();
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Internal database error at getting target user id (SQLException)."));
      return;
    } catch (UserNotFoundException e1) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Target user is not recognized."));
      return;
    } catch (InconsistentDataException e1) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Getting target user affected more than 1 row."));
      return;
    }
    assert(targetUser != null);
    int targetUserId = targetUser.getUserId();
    assert(targetUserId >= 0);
    
    // We know the user is admin, make sure the requested user is not itself
    if (currentUserId == targetUserId) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Cannot demote yourself, ask another admin to modify your role."));
      return;
    }
    
    // Looks like a valid request so far, update the database
    Response resp = new ErrorResponse("Unknown error in role update.");
      try {
        AuthLevel enumRole = AuthLevel.getAuth(req.getRole());
        db.updateUserRole(targetUserId, calendarId, enumRole);
        resp = new SuccessResponse("Updated user role.");
      } catch (CalendarSubscriptionNotFoundException e) {
        resp = new ErrorResponse("The requested update subscription does not exist.");
      } catch (InconsistentDataException e) {
        resp = new ErrorResponse("The update request affected more than 1 row.");
      } catch (SQLException e) {
        e.printStackTrace();
        resp = new ErrorResponse("Internal database error at role update (SQLException).");
      }
    request.setAttribute(Response.class.getSimpleName(), resp);
  }
}
