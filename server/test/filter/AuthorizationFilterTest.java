package filter;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import resp.ErrorResponse;
import resp.SessionResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

import db.DBInterface;
import exception.SessionNotFoundException;

public class AuthorizationFilterTest {
  
  @Mock
  HttpServletRequest req;
  @Mock
  HttpServletResponse resp;
  @Mock
  FilterChain chain;
  @Mock
  DBInterface db;
  
  private AuthorizationFilter filter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    filter = new AuthorizationFilter(db);
  }
  
  @Test
  public void filterReturnsAnErrorResponseWhenNoSessionIDWasFoundInBD() 
      throws SQLException, SessionNotFoundException, 
      IOException, ServletException {
    when(db.getSession(any(String.class))).thenThrow(
        new SessionNotFoundException());
    filter.doFilter(req, resp, chain);
    verify(req, times(1)).setAttribute(
        eq(SessionResponse.class.getSimpleName()), any(ErrorResponse.class));
  }
  
  @Test
  public void filterReturnsAnErrorResponseWhenDBErrorOccurs() throws 
      IOException, ServletException, SQLException, SessionNotFoundException {
    when(db.getSession(any(String.class))).thenThrow(new SQLException());
    filter.doFilter(req, resp, chain);
    verify(req, times(1)).setAttribute(
        eq(SessionResponse.class.getSimpleName()), any(ErrorResponse.class));
  }
  
  @Test
  public void filterSearchesForSessionIDInDBAndAddsAnAttributeToRequest() 
      throws SQLException, SessionNotFoundException, IOException, 
        ServletException {
    SessionResponse sessionResp = mock(SessionResponse.class);
    when(db.getSession(any(String.class))).thenReturn(sessionResp);
    filter.doFilter(req, resp, chain);
    verify(req, times(1)).setAttribute(
        SessionResponse.class.getSimpleName(), sessionResp);
  }
  
}
