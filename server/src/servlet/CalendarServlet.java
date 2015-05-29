package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.CalendarRequest;
import req.SessionRequest;
import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;
import resp.SuccessResponse;

import com.google.gson.Gson;

import db.DBInterface;
import db.InviteCodeGenerator;
import exception.CalendarNotFoundException;
import exception.InconsistentDataException;

@WebServlet
public class CalendarServlet extends HttpServlet {

  private final Gson gson;
  private final DBInterface db;
  private final InviteCodeGenerator generator = new InviteCodeGenerator();

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a calendar servlet with injected dependencies.
   * 
   * @param gson
   *          json serialiser
   * @param db
   *          database interface
   */
  public CalendarServlet(Gson gson, DBInterface db) {
    this.gson = gson;
    this.db = db;
  }

  /**
   * Get method, given the ID of the calendar, returns the remaining data about
   * the calendar: - name - creator (ID) - join enabled flag - join code
   * 
   * If start_date field in the request is set, all the events in the time
   * interval from the specified date up to two weeks later are added to the
   * response.
   * 
   * @throws IOException
   * 
   *           TODO: Add verification if a user is allowed to retrieve
   *           information about a specific calendar
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String id = request.getPathInfo().substring(1);
    if (id == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Request not RESTful enough."));
      return;
    }

    CalendarRequest calendarRequest;
    String startDate = request.getParameter("startDate");
    if (startDate != null) {
      calendarRequest = new CalendarRequest(Timestamp.valueOf(startDate),
          Integer.parseInt(id));
    } else {
      calendarRequest = new CalendarRequest(Integer.parseInt(id));
    }

    int userId = getUserId(request);
    calendarRequest.setUserId(userId);

    try {
      request.setAttribute(Response.class.getSimpleName(),
          db.getCalendar(calendarRequest));
    } catch (SQLException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Error while retireving the calendar information from the "
              + "database."));
    }
  }

  /**
   * Post method adds a new calendar to the database.
   * 
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    CalendarRequest calendarRequest = initCalendarRequest(request);
    calendarRequest.setInviteCode(generator.getInviteCode());

    if (!calendarRequest.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Calendar request is invalid."));
      return;
    }

    // Put calendar into the database and record the response.
    try {
      request.setAttribute(Response.class.getSimpleName(),
          db.putCalendar(calendarRequest));
    } catch (SQLException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Error while saving the calendar to the data base."));
    }
  }

  /**
   * Given the calendar ID, remove the corresponding calendar form the database
   * (set the active field to false).
   */
  @Override
  protected void doDelete(HttpServletRequest request,
      HttpServletResponse response) {
    String id = request.getPathInfo().substring(1);

    if (id == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "No calendar ID specified."));
      return;
    }

    Response result = null;
    try {
      db.deleteCalendar(Integer.parseInt(id));
      result = new SuccessResponse("Calendar was successfully deleted.");
    } catch (NumberFormatException e) {
      result = new ErrorResponse(
          "One of the specified dates was incorrectly formatted.");
    } catch (SQLException e) {
      result = new ErrorResponse(
          "Database error occured while deleting the calendar.");
    } catch (InconsistentDataException e) {
      result = new ErrorResponse(e.getMessage());
    } catch (CalendarNotFoundException e) {
      result = new ErrorResponse(e.getMessage());
    }
    request.setAttribute(Response.class.getSimpleName(), result);
  }

  /**
   * Given the calendar ID and new calendar data, update the database.
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) {
    request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
        "Error - PUT method of the calendar servlet not supported."));
  }

  /**
   * Serialise and initialise the calendar request object
   * 
   * @param HttpServletRequest
   *          request
   * @return CalendarRequest
   * @throws IOException
   */
  private CalendarRequest initCalendarRequest(HttpServletRequest request)
      throws IOException {
    SessionResponse sessionResponse = (SessionResponse) request
        .getAttribute(SessionResponse.class.getSimpleName());

    CalendarRequest calendarRequest;
    if (request.getMethod().equals("GET")) {
      calendarRequest = gson.fromJson(request.getParameter("data"),
          CalendarRequest.class);
    } else {
      calendarRequest = gson.fromJson(request.getReader(),
          CalendarRequest.class);
    }

    // Set userID of calendar creator.
    calendarRequest.setUserId(sessionResponse.getUserId());

    return calendarRequest;
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
    // TODO: Remove duplicate code
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
