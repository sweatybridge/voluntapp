package servlet;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import resp.LoginResponse;

import com.google.gson.Gson;

import db.DBInterface;

public class UserServletTest {

  private final Gson gson = new Gson();

  @Mock
  private DBInterface db;
  @Mock
  private HttpServletRequest req;
  @Mock
  private HttpServletResponse resp;
  @Mock
  private BufferedReader reqBody;
  @Mock
  private PrintWriter respBody;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testDoGetHttpServletRequestHttpServletResponse() {
    fail("Not yet implemented");
  }

  @Test
  public void testDoDeleteHttpServletRequestHttpServletResponse() {
    fail("Not yet implemented");
  }

  @Test
  public void doPutRegistersUserInDatabase() {
    UserServlet servlet = new UserServlet(gson, db);

    String payload =
        "payload=%7B%22email%22%3A%22han_qiao%40msn.com%22%2C%22password%22%3A%22123123%22%2C%22conf_password%22%3A%22123123%22%2C%22firstName%22%3A%22Qiao%22%2C%22lastName%22%3A%22Han%22%7D";

    try {
      when(resp.getWriter()).thenReturn(respBody);
      when(req.getReader()).thenReturn(reqBody);
      when(reqBody.readLine()).thenReturn(payload);
    } catch (IOException e1) {
      fail("Not yet implemented");
    }

    when(db.insertUser(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(true);

    try {
      servlet.doPut(req, resp);
    } catch (IOException | ServletException e) {
      fail("Not yet implemented");
    }

    verify(respBody).print(gson.toJson(new LoginResponse("test session id")));
  }

  @Test
  public void testDoPostHttpServletRequestHttpServletResponse() {
    fail("Not yet implemented");
  }

}
