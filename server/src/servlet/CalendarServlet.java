package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.CalendarRequest;
import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;

import com.google.gson.Gson;

import db.DBInterface;
import db.InviteCodeGenerator;


@WebServlet
public class CalendarServlet extends HttpServlet {

  private final Gson gson;
  private final DBInterface db;
  private final InviteCodeGenerator generator = new InviteCodeGenerator();

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a calendar servlet with injected dependencies.
   * 
   * @param gson json serialiser
   * @param db database interface
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
   * TODO: Add verification if a user is allowed to retrieve information about a specific calendar
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
    
    CalendarRequest calendarRequest = new CalendarRequest(Integer.parseInt(id));

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
   * Given the calendar ID, remove the corresponding calendar form the database.
   */
  @Override
  protected void doDelete(HttpServletRequest request,
      HttpServletResponse response) {
    request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
        "Error - DELETE method of the calendar servlet not supported."));
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
   * @param HttpServletRequest request
   * @return CalendarRequest
   * @throws IOException
   */
  private CalendarRequest initCalendarRequest(HttpServletRequest request)
      throws IOException {
    SessionResponse sessionResponse =
        (SessionResponse) request.getAttribute(SessionResponse.class
            .getSimpleName());

    CalendarRequest calendarRequest;
    if (request.getMethod().equals("GET")) {
      calendarRequest =
          gson.fromJson(request.getParameter("data"), CalendarRequest.class);
    } else {
      calendarRequest =
          gson.fromJson(request.getReader(), CalendarRequest.class);
    }

    // Set userID of calendar creator.
    calendarRequest.setUserId(sessionResponse.getUserId());

    return calendarRequest;
  }

}
