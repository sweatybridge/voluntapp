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
import resp.SessionResponse;

import com.google.gson.Gson;

import db.DBInterface;

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
    int userId = getUserId(request);
    if (userId == 0) {
      return;
    }
    Response subResp;
    try {
      subResp = db.getUsersCalendars(userId);
    } catch (SQLException e) {
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
  public void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // TODO: To keep servlets consistent this may be better being changed to
    // post?
    int userId = getUserId(request);
    if (userId == 0) {
      return;
    }
    CalendarSubscriptionRequest subReq = gson.fromJson(request.getReader(),
        CalendarSubscriptionRequest.class);
    subReq.setUserId(userId);

    Response subResp;
    try {
      subResp = db.putCalendarSubscription(subReq);
    } catch (SQLException e) {
      subResp = new ErrorResponse("Error while registering user's calendar "
          + "subscription.");
    }
    request.setAttribute(Response.class.getSimpleName(), subResp);
  }

  /**
   * Retrieve the authorization parameters from the request attribute. Generate
   * an error response when the user ID is invalid.
   * 
   * @param request
   *          sent to the server
   * @return ID of the user
   */
  private int getUserId(HttpServletRequest request) {
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
