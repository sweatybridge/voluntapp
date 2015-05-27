package servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import db.DBInterface;

public class EventSubscriptionServlet extends HttpServlet {
  
  private final Gson gson;
  private final DBInterface db;
  private static final long serialVersionUID = 1L;
  
  public EventSubscriptionServlet(Gson gson, DBInterface db) {
    this.gson = gson;
    this.db = db;
  }
  
  /**
   * Given the ID of an event, return the number of people attending the event.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    
  }
  
  /**
   * Given the ID of an event, register the user's subscription to the event.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    
  }
  
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    
  }
  
  /**
   * Given the ID of the event, delete user's subscription to the event.
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    
  }
}
