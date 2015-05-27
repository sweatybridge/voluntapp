package servlet;

import javax.servlet.http.HttpServlet;

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
  
  

}
