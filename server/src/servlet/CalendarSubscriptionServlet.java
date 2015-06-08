package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.CalendarSubscriptionRequest;
import resp.CalendarResponse;
import resp.CalendarSubscriptionResponse;
import resp.ErrorResponse;
import resp.Response;
import resp.SuccessResponse;
import resp.UserResponse;
import utils.AuthLevel;
import utils.CalendarJoinCodeIdQuery;
import utils.ServletUtils;

import chat.DynamicUpdate;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import db.CalendarIdUserIdMap;
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
    int userId = ServletUtils.getUserId(request);
    
    Response subResp;
    try {
      subResp = db.getUsersCalendars(userId);
      /* Record that a user is subscribed to given calendars. */
      CalendarIdUserIdMap map = CalendarIdUserIdMap.getInstance();
      for (CalendarResponse calendar : 
        ((CalendarSubscriptionResponse) subResp).getCalendars()) {
        map.put(calendar.getCalendarId(), userId);
      }
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
    int userId = ServletUtils.getUserId(request);
    
    CalendarSubscriptionRequest subReq = gson.fromJson(request.getReader(),
        CalendarSubscriptionRequest.class);
    
    if (subReq == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "No request body was specified."));
      return;
    }
    
    // Check if they can join the calendar
    if (canJoin(userId, subReq.getJoinCode())) {
      try {
        CalendarResponse resp = db.putCalendarSubscription(userId, subReq.getJoinCode());
        /* Register calendar ID to user ID mapping. */
        CalendarIdUserIdMap map = CalendarIdUserIdMap.getInstance();
        map.put(resp.getCalendarId(), userId);
        // Send dynamic update to the owner (creator)
        DynamicUpdate.sendCalendarJoin(resp.getUserId(), subReq);
        request.setAttribute(Response.class.getSimpleName(), resp);
      } catch (SQLException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse("Error while registering user's calendar "
            + "subscription."));
      }
    } else {
      request.setAttribute(Response.class.getSimpleName(), 
          new ErrorResponse("Cannot join the specified calendar."));
    }
  }
  
  private boolean canJoin(int userId, String joinCode) {
    if (joinCode == null) {
      return false;
    }
    int cid = db.getCalendarId(new CalendarJoinCodeIdQuery(joinCode));
    try {
      return db.isCalendarJoinable(cid);
    } catch (SQLException e) {
      System.err.println("Database error while checking if the calendar " +
      		"can be joined.");
    }
    return false;
  }
  
  /**
   * Given a calendar id in the form /api/subscription/calendar/[id], if the
   * current user has admin rights updates the given targetUserEmail (in the
   * payload) to the specified auth level.
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    Response resp = updateDeleteSubscription(request, 1);
    request.setAttribute(Response.class.getSimpleName(), resp);
  }

  /**
   * Given a calendar id in the form /api/subscription/calendar/[id], if the
   * current user has admin rights removes the given targetUserEmail (in the
   * payload). NOTE: Admins cannot remove themselves from calendars to ensure
   * that there is at least 1 admin.
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    Response resp = updateDeleteSubscription(request, 0);
    request.setAttribute(Response.class.getSimpleName(), resp);
  }
  
  private Response updateDeleteSubscription(HttpServletRequest request, int action) {
    // Get and check the calendar id from the url
    if (request.getPathInfo() == null
        || request.getPathInfo().substring(1).length() < 1) {
      return new ErrorResponse("No calendar ID specified.");
    }
    String calendarIdS = request.getPathInfo().substring(1);

    int calendarId = Integer.parseInt(calendarIdS);
    assert (calendarId >= 0);
    int currentUserId = ServletUtils.getUserId(request);
    AuthLevel currentLevel = db.authoriseUser(currentUserId, calendarId);
    
    // Get the payload data, we are looking for userId (targetUserId) and role
    CalendarSubscriptionRequest req;
    try {
      req = gson.fromJson(request.getReader(),
          CalendarSubscriptionRequest.class);
    } catch (JsonSyntaxException | JsonIOException | IOException e) {
      e.printStackTrace();
      return new ErrorResponse("Invalid delete calendar subscription payload.");
    }
    
    assert (req != null);
    // Get the target user id from email
    UserResponse targetUser;
    try {
      targetUser = db.getUser(req.getTargetUserEmail());
    } catch (SQLException e1) {
      e1.printStackTrace();
      return new ErrorResponse("Internal database error at getting target user id (SQLException).");
    } catch (UserNotFoundException e1) {
      return new ErrorResponse("Target user is not recognized.");
    } catch (InconsistentDataException e1) {
      return new ErrorResponse("Getting target user affected more than 1 row.");
    }
    assert (targetUser != null);
    int targetUserId = targetUser.getUserId();
    assert (targetUserId >= 0);
    
    // DELETE USER
    if (action == 0) {
      // We got all the data we need, try to unsubscribe user
      // Check if the current user is admin or not
      // Remember that admin cannot unsubscribe from their own calendars
      // to ensure that there is at least 1 admin
      if (currentUserId == targetUserId && currentLevel == AuthLevel.ADMIN) {
          return new ErrorResponse("Admins cannot unsubscribe from their calendars for security reasons, ask a fellow admin to remove you.");
      } else if (currentUserId != targetUserId && currentLevel != AuthLevel.ADMIN) {
        return new ErrorResponse("Insufficient rights to delete user subscription.");
      }
      
      // Everything seems to be fine, perform unsubscribe
      try {
        try {
          db.deleteCalendarSubscription(targetUserId, calendarId);
          CalendarIdUserIdMap map = CalendarIdUserIdMap.getInstance();
          map.remove(calendarId, targetUserId);
          return new SuccessResponse("User unsubscribed.");
        } catch (CalendarSubscriptionNotFoundException e) {
          return new ErrorResponse("The requested update subscription does not exist.");
        }
      } catch (SQLException e) {
        e.printStackTrace();
        return new ErrorResponse("Internal database error at deleting calendar subscription (SQLException).");
      } catch (InconsistentDataException e) {
        return new ErrorResponse("Deleting calendar subscription affected more than 1 row.");
      }
    } else if (action == 1) { // UPDATE SUBSCRIPTION
      // Make sure the user is admin of the calendar specified
      if (currentLevel != AuthLevel.ADMIN) {
        return new ErrorResponse("Insufficient rights to change user role.");
      }
      // We know the user is admin, make sure the requested user is not itself
      if (currentUserId == targetUserId) {
        return new ErrorResponse("Cannot demote yourself, ask another admin to modify your role.");
      }
      
      // Looks like a valid request so far, update the database
      try {
        AuthLevel enumRole = AuthLevel.getAuth(req.getRole());
        db.updateUserRole(targetUserId, calendarId, enumRole);
        return new SuccessResponse("Updated user role.");
      } catch (CalendarSubscriptionNotFoundException e) {
        return new ErrorResponse("The requested update subscription does not exist.");
      } catch (InconsistentDataException e) {
        return new ErrorResponse("The update request affected more than 1 row.");
      } catch (SQLException e) {
        e.printStackTrace();
        return new ErrorResponse("Internal database error at role update (SQLException).");
      }
    } // End of else if (action == 1)
    
    return new ErrorResponse("Unknown error in calendar subscription update or delete.");
  }
}
