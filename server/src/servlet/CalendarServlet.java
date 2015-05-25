package servlet;

import java.io.IOException;

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
import db.SessionManager;
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
   * the calendar:
   * - name
   * - creator (ID)
   * - creation date
   * - join enabled flag 
   * - join code
   * 
   * If start_date filed in the request is set, all the events in the time 
   * interval from the specified date up to two weeks later are added to the
   * response.
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
    // TODO: implement this
  }
  
  /**
   * Post method adds a new calendar to the database.
   * @throws IOException 
   */
  @Override
  protected void doPost(HttpServletRequest request, 
      HttpServletResponse respone) throws IOException {
    SessionResponse sessionResponse = (SessionResponse) request.getAttribute(
        SessionResponse.class.getSimpleName());
    
    CalendarRequest calendarRequest = gson.fromJson(request.getReader(),
        CalendarRequest.class);
    
    calendarRequest.setUserId(sessionResponse.getUserId());
    calendarRequest.setInviteCode(generator.getInviteCode());
    
    if (!calendarRequest.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Claendar request is invalid."));
      return;
    }
    
    // Put calendar into the database.
    
        
  }
  
  /**
   * Given the calendar ID, remove the corresponding calendar form the 
   * database.
   */
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
    // TODO: implement this
  }
  
  /**
   * Given the calendar ID and new calendar data, update the database.  
   */
  @Override 
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
    // TODO: implement this
  }
  

}
