package listener;

import java.util.EnumSet;
import java.util.logging.Logger;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.postgresql.ds.PGConnectionPoolDataSource;

import servlet.CalendarServlet;
import servlet.CalendarSubscriptionServlet;
import servlet.DefaultServlet;
import servlet.SessionServlet;
import servlet.UserServlet;

import com.google.gson.Gson;

import db.DBInterface;
import db.SessionManager;
import filter.AuthorizationFilter;
import filter.JsonFilter;

/**
 * Main application context that maps servlets to their respective URI and
 * injects appropriate dependencies to servlet constructors, for eg. json
 * serialiser and database interface. Uses database connection pool managed by
 * container instead of single connection.
 */
@WebListener
public class Application implements ServletContextListener {

  public static final Logger logger = Logger.getLogger("Logs'R'Us");

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext context = sce.getServletContext();

    // Using connection pool managed by servlet container
    PGConnectionPoolDataSource source = new PGConnectionPoolDataSource();
    source.setUrl(context.getInitParameter("db_host"));
    source.setUser(context.getInitParameter("db_user"));
    source.setPassword(context.getInitParameter("db_pass"));
    source.setSsl(true);
    source.setSslfactory("org.postgresql.ssl.NonValidatingFactory");

    // Reusable objects for all servlets (must be thread safe).
    Gson gson = new Gson();
    DBInterface db = new DBInterface(source);
    SessionManager sm = new SessionManager(db);

    // Initialise filters
    context.addFilter(AuthorizationFilter.class.getSimpleName(),
        new AuthorizationFilter(db)).addMappingForUrlPatterns(
        EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), true,
        "/api/*");
    context.addFilter(JsonFilter.class.getSimpleName(), new JsonFilter(gson))
        .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true,
            "/api/*");

    // Instantiate servlets and add mappings
    context.addServlet(DefaultServlet.class.getName(), new DefaultServlet(db))
        .addMapping("");
    context.addServlet(UserServlet.class.getName(), new UserServlet(gson, db))
        .addMapping("/api/user");
    context.addServlet(SessionServlet.class.getName(),
        new SessionServlet(gson, db, sm)).addMapping("/api/session");
    context.addServlet(CalendarServlet.class.getName(),
        new CalendarServlet(gson, db)).addMapping("/api/calendar");
    context.addServlet(CalendarSubscriptionServlet.class.getName(),
        new CalendarSubscriptionServlet(gson, db)).addMapping("/api/calendarSubscription");
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
  }

}
