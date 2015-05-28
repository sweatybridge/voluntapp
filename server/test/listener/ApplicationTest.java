package listener;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRegistration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import filter.AuthorizationFilter;
import filter.JsonFilter;

public class ApplicationTest {

  @Mock
  private ServletContextEvent sce;
  @Mock
  private ServletContext ctx;
  @Mock
  private FilterRegistration.Dynamic filterReg;
  @Mock
  private ServletRegistration.Dynamic servletReg;


  private Application app;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    app = new Application();
  }

  @Test
  public void appListenerMapsAuthorizationFilterBeforJsonFilter() {
    when(sce.getServletContext()).thenReturn(ctx);
    when(ctx.getInitParameter("db_host")).thenReturn(
        "jdbc:postgresql://db.doc.ic.ac.uk/");
    when(ctx.addFilter(any(String.class), any(Filter.class))).thenReturn(
        filterReg);
    when(ctx.addServlet(any(String.class), any(Servlet.class))).thenReturn(
        servletReg);

    app.contextInitialized(sce);

    InOrder inOrder = inOrder(ctx);
    inOrder.verify(ctx).addFilter(
        eq(AuthorizationFilter.class.getSimpleName()),
        any(AuthorizationFilter.class));
    inOrder.verify(ctx).addFilter(eq(JsonFilter.class.getSimpleName()),
        any(JsonFilter.class));
  }

  @Test
  public void contextDestroyedDoesNothing() {
    app.contextDestroyed(sce);
  }

}
