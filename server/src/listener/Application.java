package listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import servlet.DefaultServlet;
import servlet.UserServlet;

import com.google.gson.Gson;

import db.DBInterface;

/**
 * Main application context that maps servlets to their respective URI and
 * injects appropriate dependencies to servlet constructors, for eg. json
 * serialiser and database interface.
 */
@WebListener
public class Application implements ServletContextListener {

  /**
   * Reusable objects for all servlets (must be thread safe).
   */
  private static final Gson gson = new Gson();
  private static final DBInterface db = new DBInterface();

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext context = sce.getServletContext();

    // Instantiate servlets and add mappings
    context.addServlet(DefaultServlet.class.getName(), new DefaultServlet(db))
        .addMapping("");
    context.addServlet(UserServlet.class.getName(), new UserServlet(gson, db))
        .addMapping("/user");
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    /* Required by context listener interface */
  }
}
