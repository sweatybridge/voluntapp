package resp;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

import org.postgresql.util.PGInterval;

/**
 * A successful response to a event request.
 */
public class EventResponse extends Response {

  public static String EID_COLUMN = "EID";
  public static String TITLE_COLUMN = "TITLE";
  public static String DESC_COLUMN = "DESCRIPTION";
  public static String LOCATION_COLUMN = "LOCATION";
  public static String DATE_COLUMN = "DATE";
  public static String TIME_COLUMN = "TIME";
  public static String DURATION_COLUMN = "DURATION";
  public static String MAX_ATTEDEE_COLUMN = "MAX_ATTENDEES";
  public static String ACTIVE_COLUMN = "ACTIVE";

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
  private int max = -1;

  /**
   * Other variables used by the database interface
   */
  private transient ResultSet rs;
  private transient PGInterval sqlDuration;
  private transient Date sqlDate;
  private transient Time sqlTime;
  private transient boolean found;
  private transient boolean delete;

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
      String time, String date, String duration, String max, int eventId,
      int calendarId) {
    this.title = title;
    this.description = description;
    this.location = location;
    this.duration = duration;
    this.time = time;
    this.date = date;
    this.eventId = eventId;
    this.calendarId = calendarId;
    if (max == null) {
      this.max = -1;
    } else {
      this.max = Integer.parseInt(max);
    }
  }

  public EventResponse(String title, String description, String location,
      String time, String date, String duration, String max, int eventId,
      int calendarId, boolean delete) {
    this(title, description, location, time, date, duration, max, eventId,
        calendarId);
    this.delete = delete;
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
    this.max = rs.getInt(MAX_ATTEDEE_COLUMN);
  }

  @Override
  public String getSQLUpdate() {
    int found = 0;
    String formatString = ((title == null || found++ == Integer.MIN_VALUE) ? ""
        : "\"" + TITLE_COLUMN + "\"='" + title + "',")
        + ((description == null || found++ == Integer.MIN_VALUE) ? "" : "\""
            + DESC_COLUMN + "\"='" + description + "',")
        + ((location == null || found++ == Integer.MIN_VALUE) ? "" : "\""
            + LOCATION_COLUMN + "\"='" + location + "',")
        + ((date == null || found++ == Integer.MIN_VALUE) ? "" : "\""
            + DATE_COLUMN + "\"='" + date + "',")
        + ((time == null || found++ == Integer.MIN_VALUE) ? "" : "\""
            + TIME_COLUMN + "\"='" + time + "',")
        + ((duration == null || found++ == Integer.MIN_VALUE) ? "" : "\""
            + DURATION_COLUMN + "\"='" + duration + "',")
        + ((max == -1 || found++ == Integer.MIN_VALUE) ? "" : "\""
            + MAX_ATTEDEE_COLUMN + "\"=" + max + ",")
        + ((!delete || found++ == Integer.MIN_VALUE) ? "" : "\""
            + ACTIVE_COLUMN + "\"" + "=false,");
    return (found == 0) ? null : String.format(
        "UPDATE public.\"EVENT\" SET %s WHERE \"EID\"=%d",
        formatString.substring(0, formatString.length() - 1), eventId);
  }

  @Override
  public void checkResult(int rowsAffected) {
    // TODO Auto-generated method stub
  }

  @Override
  public String getSQLInsert() {
    return String
        .format(
            "WITH x AS (INSERT INTO public.\"EVENT\" VALUES (DEFAULT, '%s', '%s', '%s', '%s', '%s', '%s', %s, true) RETURNING \"EID\") INSERT INTO public.\"CALENDAR_EVENT\" SELECT %d,\"EID\" FROM x;",
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
