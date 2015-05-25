package servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.Gson;

import db.DBInterface;
import db.SessionManager;

public abstract class ServletTest {

  protected static final int TEST_USER_ID = 7;
  protected static final String TEST_EMAIL = "james.bond@gmail.com";
  protected static final String TEST_PASSWORD = "123456";
  protected static final String TEST_PASSWORD_HASHED = "123456";
  protected static final String TEST_PASSWORD_WRONG = "654321";
  protected static final String TEST_FIRST_NAME = "James";
  protected static final String TEST_LAST_NAME = "Bond";

  protected static final String TEST_SESSION_ID = "111";

  @Mock
  protected HttpServletRequest req;
  @Mock
  protected HttpServletResponse resp;
  @Mock
  protected DBInterface db;
  @Mock
  protected SessionManager sm;

  protected Gson gson = new Gson();

  @Before
  public void init() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

}
