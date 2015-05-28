package resp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import sql.SQLInsert;
import sql.SQLQuery;

/**
 * A successful response to a calendar request.
 */
public class CalendarResponse extends Response implements SQLInsert, SQLQuery {

  public static String CID_COLUMN = "ID";
  public static String CNAME_COLUMN = "NAME";
  public static String CREATOR_COLUMN = "CREATOR";
  public static String CREATED_COLUMN = "CREATED";
  public static String JOIN_ENABLED_COLUMN = "JOIN_ENABLED";
  public static String JOIN_CODE_COLUMN = "JOIN_CODE";
  public static String ACTIVE_COLUMN = "ACTIVE";

  /**
   * Calendar details returned to client.
   */
  private int calendarId;
  private String name;
  private boolean joinEnabled;
  private String joinCode;
  private List<EventResponse> events;
  /**
   * Fields excluded from deserialisation.
   */
  private transient ResultSet rs;
  private transient Timestamp creationDate;
  private transient int userId;
  private transient boolean active;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public CalendarResponse() {
  }

  public CalendarResponse(int calendarId) {
    this.calendarId = calendarId;
  }

  public CalendarResponse(String name, boolean joinEnabled, int userId,
      String joinCode) {
    this.name = name;
    this.joinEnabled = joinEnabled;
    this.userId = userId;
    this.joinCode = joinCode;
  }

  public int getCalendarId() {
    return calendarId;
  }

  @Override
  public String getSQLInsert() {
    return String.format("INSERT INTO public.\"CALENDAR\" VALUES "
        + "(DEFAULT, '%s', '%d', DEFAULT, %b, '%s', DEFAULT);",
        name.replace("\'", "\'\'"), userId, joinEnabled, joinCode.replace("\'", "\'\'"));
  }

  @Override
  public String getSQLQuery() {
    return String.format("SELECT \"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\" FROM "
        + "\"CALENDAR\" WHERE \"ID\"='%d' AND \"%s\"=true;", CNAME_COLUMN, 
        CREATOR_COLUMN, CREATED_COLUMN, JOIN_ENABLED_COLUMN, JOIN_CODE_COLUMN, 
        ACTIVE_COLUMN, calendarId, ACTIVE_COLUMN);
  }

  public List<EventResponse> getCalendarEvents() {
    return events;
  }

  @Override
  public void setResult(ResultSet result) {
    this.rs = result;
    try {
      rs.next();
      setCalendarResponse();
    } catch (SQLException e) {
      System.err.println("Error getting the result while creating "
          + "calendarResponse object.");
      return;
    }
  }

  private void setCalendarResponse() throws SQLException {
    name = rs.getString(CNAME_COLUMN);
    userId = rs.getInt(CREATOR_COLUMN);
    creationDate = rs.getTimestamp(CREATED_COLUMN);
    joinEnabled = rs.getBoolean(JOIN_ENABLED_COLUMN);
    joinCode = rs.getString(JOIN_CODE_COLUMN);
    active = rs.getBoolean(ACTIVE_COLUMN);
  }

  public void setCalendarID(int id) {
    this.calendarId = id;
  }

  public void setEvents(List<EventResponse> events) {
    this.events = events;
  }

  public void addEvent(EventResponse event) {
    events.add(event);
  }

  public static void main(String[] args) {
    CalendarResponse resp = new CalendarResponse(123);
    System.out.println(resp.getSQLQuery());
  }
}
