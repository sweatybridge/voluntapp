package servlet;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.CalendarRequest;
import resp.CalendarResponse;
import resp.ErrorResponse;
import resp.Response;
import resp.SuccessResponse;
import utils.ServletUtils;

import chat.DynamicUpdate;

import com.google.gson.Gson;

import db.CalendarIdUserIdMap;
import db.CodeGenerator;
import db.DBInterface;
import exception.CalendarNotFoundException;
import exception.InconsistentDataException;

@WebServlet
public class CalendarServlet extends HttpServlet {

  private final Gson gson;
  private final DBInterface db;
  private final CodeGenerator generator = new CodeGenerator();

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
    int userId = ServletUtils.getUserId(request);
    String startDate = request.getParameter("startDate");
    if (startDate != null) {
      calendarRequest = new CalendarRequest(userId,
          Timestamp.valueOf(startDate), Integer.parseInt(id));
    } else {
      calendarRequest = new CalendarRequest(userId, Integer.parseInt(id));
    }

    try {
      CalendarResponse calendar = db.getCalendar(calendarRequest);
      if (calendar == CalendarResponse.NO_CALENDAR) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "You do not have access to this calendar."));
        return;
      }
      request.setAttribute(Response.class.getSimpleName(), calendar);
    } catch (SQLException e) {
      e.printStackTrace();
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
    calendarRequest.setInviteCode(generator.getCode());

    if (!calendarRequest.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Calendar request is invalid."));
      return;
    }

    // Put calendar into the database and record the response.
    try {
      CalendarResponse resp = db.putCalendar(calendarRequest);
      request.setAttribute(Response.class.getSimpleName(), resp);
      /* Register calendar ID to user ID mapping. */
      Integer userId = ServletUtils.getUserId(request);
      CalendarIdUserIdMap map = CalendarIdUserIdMap.getInstance();
      map.put(resp.getCalendarId(), userId);
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

    // See if the user can access the operation
    switch (db.authoriseUser(ServletUtils.getUserId(request),
        Integer.parseInt(id))) {
    case NONE:
    case BASIC:
      result = new ErrorResponse("Operation not allowed (Delete calendar)");
      response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
      request.setAttribute(Response.class.getSimpleName(), result);
      return;
    default:
      break;
    }

    try {
      Integer calendarId = Integer.parseInt(id);
      CalendarResponse resp = db.deleteCalendar(calendarId);
      
      // Remove fields that we do not need for notification
      resp.setJoinEnabled(null);
      resp.setJoinCode(null);
      DynamicUpdate.sendCalendarDelete(calendarId, resp);
      
      /* Delete calendar ID to user ID mapping. */
      CalendarIdUserIdMap map = CalendarIdUserIdMap.getInstance();
      map.remove(calendarId);
      
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
   * 
   * @throws IOException
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    int userId = ServletUtils.getUserId(request);
    String id = request.getPathInfo().substring(1);

    if (id == null) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "No calendar ID specified."));
      return;
    }

    CalendarRequest calendarRequest = gson.fromJson(request.getReader(),
        CalendarRequest.class);

    /*
     * Check if joining the calendar was re-enabled. If so, generate new join
     * code for the calendar.
     */
    int cid = Integer.parseInt(id);
    calendarRequest.setUserId(userId);
    calendarRequest.setCalendarId(cid);
    if (calendarRequest.isJoinEnabled()) {
      try {
        CalendarResponse resp = db.getCalendar(calendarRequest);
        if (resp != CalendarResponse.NO_CALENDAR && !resp.getJoinEnabled()) {
          calendarRequest.setInviteCode(generator.getCode());
        }
      } catch (SQLException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Data base error occured."));
        return;
      }
    }

    // Check that the calendar can be updated by the user
    Response result;
    switch (db.authoriseUser(ServletUtils.getUserId(request), cid)) {
    case NONE:
    case BASIC:
    case EDITOR:
      result = new ErrorResponse("Operation not allowed (Update calendar)");
      response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
      request.setAttribute(Response.class.getSimpleName(), result);
      return;
    default:
      break;
    }

    try {
      CalendarResponse resp = db.updateCalendar(cid, calendarRequest);
      // Remove fields that we don't want to expose
      resp.setJoinEnabled(null);
      resp.setJoinCode(null);
      DynamicUpdate.sendCalendarUpdate(cid, resp);
      result = new SuccessResponse("Calendar data was successfully updated.");
    } catch (NumberFormatException e) {
      result = new ErrorResponse("One of the specified dates was invalid.");
    } catch (SQLException e) {
      e.printStackTrace();
      result = new ErrorResponse(
          "Database error occured while updating the calendar.");
    } catch (InconsistentDataException e) {
      result = new ErrorResponse(e.getMessage());
    } catch (CalendarNotFoundException e) {
      result = new ErrorResponse(e.getMessage());
    }
    request.setAttribute(Response.class.getSimpleName(), result);
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
    int userId = ServletUtils.getUserId(request);

    CalendarRequest calendarRequest;
    if (request.getMethod().equals("GET")) {
      calendarRequest = gson.fromJson(request.getParameter("data"),
          CalendarRequest.class);
    } else {
      calendarRequest = gson.fromJson(request.getReader(),
          CalendarRequest.class);
    }

    // Set userID of calendar creator.
    calendarRequest.setUserId(userId);

    return calendarRequest;
  }
}
