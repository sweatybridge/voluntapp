package servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import req.EventRequest;
import resp.ErrorResponse;
import resp.EventResponse;
import resp.Response;
import resp.SuccessResponse;

import com.google.gson.Gson;

import db.DBInterface;
import exception.EventNotFoundException;
import exception.InconsistentDataException;

public class EventServlet extends HttpServlet {
  
  private final Gson gson;
  private final DBInterface db;
  private static final long serialVersionUID = 1L;
  
  public EventServlet(Gson gson, DBInterface db) {
    this.gson = gson;
    this.db = db;
  }

  /**
   * Given the ID of the event, return all the information about the event.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    
  }
  
  /**
   * Add the event to the database.
   * @throws IOException 
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    EventRequest eventReq = 
        gson.fromJson(request.getReader(), EventRequest.class);
    
    if (!eventReq.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), 
          new ErrorResponse("The supplied event data are invalid."));
      return;
    }
    
    try {
      EventResponse resp = db.putEvent(eventReq);
      request.setAttribute(Response.class.getSimpleName(), resp);
    } catch (SQLException e) {
      request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
          "Error occurred while adding a new event to the database."));
    }
  }
  
  /**
   * Given the ID of the event, update the specified data of the event.
   * @throws IOException 
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    EventRequest eventReq = 
        gson.fromJson(request.getReader(), EventRequest.class);
    
    if (!eventReq.isValid()) {
      request.setAttribute(Response.class.getSimpleName(), 
          "The updated event data are invalid.");
    }
    
    String eventId = eventReq.getEventId();
    if (eventId != null) {    
      try {
        if (!db.updateEvent(Integer.parseInt(eventId), eventReq)) {
          request.setAttribute(Response.class.getSimpleName(), 
              new ErrorResponse("Update of the event data was not successful."));
        } else {
          request.setAttribute(Response.class.getSimpleName(), 
              "Event data were updated successfully.");
        }
      } catch (NumberFormatException e) {
        request.setAttribute(Response.class.getSimpleName(), 
            new ErrorResponse(
                "One of the provided dates was incorrectly formatted."));
      } catch (SQLException e) {
        request.setAttribute(Response.class.getSimpleName(), 
            new ErrorResponse(
                "Error while saving the event update to the database."));
      } catch (EventNotFoundException e) {
        request.setAttribute(Response.class.getSimpleName(), 
            new ErrorResponse(
                "No event with specified event ID exists in the database."));
      } catch (InconsistentDataException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Update left the database in an inconsistent state, " +
            "more than one row was updated."));
      }
    } else {
      request.setAttribute(Response.class.getSimpleName(), 
          "No event ID was specified.");
    }
  }
  
  /**
   * Delete an event with the corresponding ID.
   * @throws IOException 
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
    EventRequest eventReq = 
        gson.fromJson(request.getReader(), EventRequest.class);
    
    String eventId = eventReq.getEventId();
    if (eventId != null) {
      try {
        db.deleteEvent(Integer.parseInt(eventId));
        request.setAttribute(Response.class.getSimpleName(), 
            new SuccessResponse("The event was successfully deleted."));
      } catch(EventNotFoundException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "No event with specified event ID exists in the database."));
      } catch(SQLException e) {
        request.setAttribute(Response.class.getSimpleName(), 
            new ErrorResponse(
                "Error while deleting the data from the database."));
      } catch(InconsistentDataException e) {
        request.setAttribute(Response.class.getSimpleName(), new ErrorResponse(
            "Update left the database in an inconsistent state, " +
            "more than one row was deleted."));
      }
    }
  }
  
}
