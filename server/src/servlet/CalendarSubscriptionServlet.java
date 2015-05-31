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
import utils.ServletUtils;

import com.google.gson.Gson;

import db.DBInterface;
import exception.InconsistentDataException;

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
    if (userId == 0) {
      return;
    }
    Response subResp;
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
    // TODO: To keep servlets consistent this may be better being changed to
    // post?
    int userId = ServletUtils.getUserId(request);
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
}
