package listener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import servlet.DefaultServlet;
import servlet.SessionServlet;
import servlet.UserServlet;

import com.google.gson.Gson;

import db.DBInterface;
import filter.AuthorizationFilter;
import db.SessionManager;

/**
 * Main application context that maps servlets to their respective URI and
 * injects appropriate dependencies to servlet constructors, for eg. json
 * serialiser and database interface.
 */
@WebListener
public class Application implements ServletContextListener {

  private static final Logger logger = Logger.getLogger("Application");

  private Connection conn;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext context = sce.getServletContext();

    try {
      // Establish database connection
      conn =
          DriverManager.getConnection(context.getInitParameter("db_host"),
              context.getInitParameter("db_user"),
              context.getInitParameter("db_pass"));

      // Reusable objects for all servlets (must be thread safe).
      Gson gson = new Gson();
      DBInterface db = new DBInterface(conn);
      SessionManager sm = new SessionManager(db);

      // Instantiate servlets and add mappings
      context
          .addServlet(DefaultServlet.class.getName(), new DefaultServlet(db))
          .addMapping("");
      context
          .addServlet(UserServlet.class.getName(), new UserServlet(gson, db))
          .addMapping("/user");
      context.addServlet(SessionServlet.class.getName(),
          new SessionServlet(gson, db, sm)).addMapping("/session");
      // Instantiate authorization filter
      context
          .addFilter(AuthorizationFilter.class.getName(), 
              new AuthorizationFilter(db));

    } catch (SQLException e) {
      // Shuts down server if any error occur during context initialisation
      logger.log(Level.SEVERE, "Failed to establish connection with database.");
      System.exit(1);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    try {
      conn.close();
    } catch (SQLException e) {
      logger.log(Level.WARNING, "Failed to close connection with database.");
    }
  }
}
