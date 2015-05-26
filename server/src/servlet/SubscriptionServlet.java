package servlet;

import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import resp.ErrorResponse;
import resp.Response;
import resp.SessionResponse;

import com.google.gson.Gson;

import db.DBInterface;

@WebServlet
public class SubscriptionServlet extends HttpServlet {
  
  private static final long serialVersionUID = 1L;
  
  private final DBInterface db;
  private final Gson gson;
  
  public SubscriptionServlet(Gson gson, DBInterface db) {
    this.db = db;
    this.gson = gson;
  }
  
  /**
   * Given the ID of the user (retrieved from the attribute of the request)
   * get the IDs of the calendars to which the user subscribed.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    SessionResponse sessionResponse = (SessionResponse) 
        request.getAttribute(SessionResponse.class.getSimpleName());
    
    /* No valid userId supplied. */
    if (sessionResponse.getUserId() == 0) {
      request.setAttribute(Response.class.getSimpleName(), 
          new ErrorResponse("Error - no user ID supplied."));
      return;
    }
    
    Response subResp;
    try {
      subResp = db.getUsersCalendars(sessionResponse.getUserId());
    } catch (SQLException e) {
      subResp = new ErrorResponse("Error while retirieving the calendar IDs " +
      		"from the database.");
    }
    
    request.setAttribute(Response.class.getSimpleName(), subResp);
  }
}
