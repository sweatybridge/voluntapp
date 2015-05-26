package req;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import resp.EventResponse;
import sql.SQLQuery;


/**
 * Deserialized JSON object of an API request to create a calendar.
 */
public class CalendarRequest implements Request {

  /**
   * Invalid register request object to replace null checks.
   */
  public static final CalendarRequest INVALID = new CalendarRequest();
  
  /**
   * Length of time interval (in miliseconds). Events in the specified time
   * interval [startDate, startDate + TIME_INTERVAL] are going to be returned
   * on calendar query. 
   */
  public static final int TIME_INTERVAL = 14 * 24 * 60 * 60 * 1000;
  
  /**
   * Calendar details sent by the client.
   */
  private String name;
  private Integer calendarId;
  private boolean joinEnabled;
  private Timestamp startDate;  

  /**
   * Fields excluded from deserialisation.
   */
  private transient String inviteCode;
  private transient int userId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public CalendarRequest() {}

  public CalendarRequest(String name, boolean joinEnabled, String inviteCode,
      int userId) {
    this.name = name;
    this.joinEnabled = joinEnabled;
    this.inviteCode = inviteCode;
    this.userId = userId;
  }
  
  /* Constructor added for testing. */
  public CalendarRequest(Timestamp startDate, int calendarId) {
    this.startDate = startDate;
    this.calendarId = calendarId;
  }
  
  @Override
  public boolean isValid() {
    return (name != null && !name.isEmpty());
  }
  
  public void setUserId(int userId) {
    this.userId = userId;
  }
  
  public void setInviteCode(String inviteCode) {
    this.inviteCode = inviteCode;
  }

  public String getName() {
    return name;
  }

  public boolean isJoinEnabled() {
    return joinEnabled;
  }

  public String getInviteCode() {
    return inviteCode;
  }

  public int getUserId() {
    return userId;
  }
  
  public Integer getCalendarId() {
    return calendarId;
  }

  public Timestamp getStartDate() {
    return startDate;
  }
  
  public CalendarEventsQuery getCalendarEventsQuery() {
    return new CalendarEventsQuery();
  }
  
  public class CalendarEventsQuery implements SQLQuery {
    private ResultSet rs;
    private List<EventResponse> events = new ArrayList<EventResponse>();
    
    @Override
    public String getSQLQuery() {
      Timestamp endDate = new Timestamp(startDate.getTime() + TIME_INTERVAL);
      return String.format("SELECT * FROM \"EVENT\" " +
      		"WHERE (\"DATE\" + \"TIME\", \"DATE\" + \"TIME\" + \"DURATION\") " +
      		"OVERLAPS ('%s', '%s') " +
      		"AND " +
      		"\"EID\" IN (SELECT \"EID\" FROM \"CALENDAR_EVENT\" WHERE \"CID\"=%d);", 
      		startDate, endDate, calendarId);
    }

    @Override
    public void setResult(ResultSet result) {
      this.rs = result;
      try {
        while(rs.next()) {
          events.add(new EventResponse(
              rs.getString(EventResponse.TITLE_COLUMN), 
              rs.getString(EventResponse.DESC_COLUMN), 
              rs.getString(EventResponse.LOCATION_COLUMN), 
              rs.getString(EventResponse.DATE_COLUMN), 
              rs.getString(EventResponse.TIME_COLUMN), 
              rs.getString(EventResponse.DURATION_COLUMN), 
              rs.getString(EventResponse.MAX_ATTEDEE_COLUMN),
              rs.getInt(EventResponse.EID_COLUMN), 
              calendarId));
        }
      } catch (SQLException e) {
        System.err.println("Error getting the result while creating " +
            "calendarResponse object.");
        return;
      }
    }
    
    public List<EventResponse> getEvents() {
      return events;
    }
  }
  
  /*public static void main(String[] args) {
    @SuppressWarnings("deprecation")
    CalendarRequest req = new CalendarRequest(new Timestamp(94, 3, 14, 1, 1, 1, 1));
    System.out.println(req.getCalendarEventsQuery().getSQLQuery());
  }*/
}
