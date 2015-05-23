package resp;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

import org.postgresql.util.PGInterval;

import sql.SQLInsert;
import sql.SQLQuery;
import sql.SQLUpdate;

/**
 * A successful response to a event request.
 */
public class EventResponse extends Response implements SQLQuery, SQLInsert,
    SQLUpdate {

  public static String EID_COLUMN = "EID";
  private static String TITLE_COLUMN = "TITLE";
  private static String DESC_COLUMN = "DESCRIPTION";
  private static String LOCATION_COLUMN = "LOCATION";
  private static String DATE_COLUMN = "DATE";
  private static String TIME_COLUMN = "TIME";
  private static String DURATION_COLUMN = "DURATION";
  private static String ATTENDEE_COLUMN = "ATTENSEES";
  private static String MAX_ATTEDEE_COLUMN = "MAX_ATTENDEES";
  private static String ACTIVE_COLUMN = "ACTIVE";

  /**
   * Event details returned to the client.
   */
  private int eventId;
  private String title;
  private String description;
  private String location;
  private String time; // HH:MM
  private String date; // YY-MM-DD
  private String duration;

  /**
   * Other variables used by the database interface
   */
  private transient ResultSet rs;
  private transient PGInterval sqlDuration;
  private transient Date sqlDate;
  private transient Time sqlTime;
  private transient int[] attendees;
  private transient int max;
  private transient boolean found;

  /**
   * Fields excluded from serialisation.
   */
  private transient int calendarId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public EventResponse() {
  }

  /**
   * Constructs a successful event response.
   * 
   * @param email
   *          Email of the user response
   * @param hashedPassword
   *          Password found in the database
   * @param userId
   *          The ID of the user requests
   */
  public EventResponse(String title, String description, String location,
      String time, String date, int calendarId, String duration) {
    this.title = title;
    this.description = description;
    this.location = location;
    this.duration = duration;
    this.time = time;
    this.date = date;
    this.calendarId = calendarId;
  }

  public int getCalendarId() {
    return calendarId;
  }

  private void setEventResponse() throws SQLException {
    this.eventId = rs.getInt(EID_COLUMN);
    this.title = rs.getString(TITLE_COLUMN);
    this.description = rs.getString(DESC_COLUMN);
    this.location = rs.getString(LOCATION_COLUMN);
    this.sqlTime = (Time) rs.getObject(TIME_COLUMN);
    this.sqlDate = (Date) rs.getObject(DATE_COLUMN);
    this.sqlDuration = (PGInterval) rs.getObject(DURATION_COLUMN);
    this.attendees = (int[]) rs.getArray(ATTENDEE_COLUMN).getArray();
    this.max = rs.getInt(MAX_ATTEDEE_COLUMN);
  }

  @Override
  public String getSQLUpdate() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void checkResult(int rowsAffected) {
    // TODO Auto-generated method stub
  }

  @Override
  public String getSQLInsert() {
    return String
        .format(
            "  WITH x AS (INSERT INTO public.\"EVENTS\" VALUES (DEFAULT, '%s', '%s', '%s', '%s', '%s', '%s', '{}', %s, true) RETURNING \"EID\") INSERT INTO public.\"CALENDAR_EVENT\" SELECT %d,\"EID\" FROM x; ",
            title, description, location, date, (time == null) ? "DEFAULT"
                : time, (duration == null) ? "DEFAULT" : duration, max,
            calendarId);
  }

  @Override
  public String getSQLQuery() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setResult(ResultSet result) {
    this.rs = result;
    try {
      found = rs.next();
      setEventResponse();
    } catch (SQLException e) {
      System.err.println("Error getting the result");
      return;
    }
  }
}
