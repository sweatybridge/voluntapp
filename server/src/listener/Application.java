package listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import servlet.UserServlet;

import com.google.gson.Gson;

import db.DBInterface;

@WebListener
public class Application implements ServletContextListener {

  private static final Gson gson = new Gson();
  private static final DBInterface db = new DBInterface();

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContext context = sce.getServletContext();
    context.addServlet(UserServlet.class.getName(), new UserServlet(gson, db))
        .addMapping("/user");
  }

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {
    // TODO Auto-generated method stub

  }
}
