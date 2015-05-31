package resp;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.validator.routines.CalendarValidator;
import org.postgresql.util.PGInterval;

/**
 * A successful response to a event request.
 */
public class EventResponse extends Response {

  private static final SimpleDateFormat UTC_FORMATTER = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss");

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
   * Event details returned to the client. Always in UTC.
   */
  private int eventId = -1;
  private String title;
  private String description;
  private String location;
  private String duration; // HH:mm
  private String startDateTime;
  private String endDateTime;
  private int currentCount = -1;
  private int max = -1;
  private boolean hasJoined = false;

  /**
   * Other variables used by the database interface
   */
  private transient ResultSet rs;
  private transient PGInterval sqlDuration;
  private transient Date sqlDate;
  private transient Time sqlTime;
  private transient boolean found;
  private transient boolean delete;
  private transient String startTime; // HH:mm
  private transient String startDate; // YYYY-MM-DD

  /**
   * Fields excluded from serialisation.
   */
  private transient int calendarId = -1;

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
      Calendar startDateTime, Calendar endDateTime, String max, int eventId,
      int calendarId) {
    this.title = title;
    this.description = description;
    this.location = location;
    this.eventId = eventId;
    this.calendarId = calendarId;
    if (max == null) {
      this.max = -1;
    } else {
      this.max = Integer.parseInt(max);
    }

    // Parse calendar to sql date time and pginterval for storage
    long start = startDateTime.getTimeInMillis();
    this.sqlDate = new Date(start);
    this.sqlTime = new Time(start);
    this.sqlDuration = new PGInterval(0, 0, endDateTime.get(Calendar.DATE)
        - startDateTime.get(Calendar.DATE),
        endDateTime.get(Calendar.HOUR_OF_DAY)
            - startDateTime.get(Calendar.HOUR_OF_DAY),
        endDateTime.get(Calendar.MINUTE) - startDateTime.get(Calendar.MINUTE),
        0);
  }

  /**
   * Constructor for deleting event.
   * 
   * @param eventId
   * @param delete
   */
  public EventResponse(int eventId, boolean delete) {
    this.eventId = eventId;
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

    // Fill in composite fields
    java.util.Date start = new java.util.Date(sqlDate.getTime()
        + sqlTime.getTime());
    this.startDateTime = UTC_FORMATTER.format(start).concat("Z");
    sqlDuration.add(start);
    this.endDateTime = UTC_FORMATTER.format(start).concat("Z");
  }

  /*
   * TODO: Change the update query to allow partial updates, i.e. updates of
   * only a subset of the row fields.
   */
  @Override
  public String getSQLUpdate() {
    int found = 0;
    String formatString = ((title == null || found++ == Integer.MIN_VALUE) ? ""
        : "\"" + TITLE_COLUMN + "\"='" + title + "',")
        + ((description == null || found++ == Integer.MIN_VALUE) ? "" : "\""
            + DESC_COLUMN + "\"='" + description.replace("\'", "\'\'") + "',")
        + ((location == null || found++ == Integer.MIN_VALUE) ? "" : "\""
            + LOCATION_COLUMN + "\"='" + location.replace("\'", "\'\'") + "',")
        + ((startDate == null || found++ == Integer.MIN_VALUE) ? "" : "\""
            + DATE_COLUMN + "\"='" + startDate.replace("\'", "\'\'") + "',")
        + ((startTime == null || found++ == Integer.MIN_VALUE) ? "" : "\""
            + TIME_COLUMN + "\"='" + startTime.replace("\'", "\'\'") + "',")
        + ((duration == null || found++ == Integer.MIN_VALUE) ? "" : "\""
            + DURATION_COLUMN + "\"='" + duration.replace("\'", "\'\'") + "',")
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

  /*
   * @Override public String getSQLInsert() { return String .format(
   * "WITH x AS (INSERT INTO public.\"EVENT\" VALUES (DEFAULT, '%s', '%s', '%s', '%s', '%s', '%s', %s, true) RETURNING \"EID\") INSERT INTO public.\"CALENDAR_EVENT\" SELECT %d,\"EID\" FROM x;"
   * , title.replace("\'", "\'\'"), description.replace("\'", "\'\'"),
   * location.replace("\'", "\'\'"), startDate.replace("\'", "\'\'"), (startTime
   * == null) ? "DEFAULT" : startTime.replace("\'", "\'\'"), (duration == null)
   * ? "DEFAULT" : duration.replace("\'", "\'\'"), max, calendarId); }
   */

  @Override
  public String getSQLInsert() {
    return String
        .format(
            "WITH x AS (INSERT INTO \"EVENT\" VALUES (DEFAULT, ?, ?, ?, ?, %s, %s, ?, true) RETURNING \"EID\") INSERT INTO \"CALENDAR_EVENT\"  SELECT %d,\"EID\" FROM x;",
            (sqlTime == null) ? "DEFAULT" : "?",
            (sqlDuration == null) ? "DEFAULT" : "?", calendarId);
  }

  @Override
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {
    prepared.setString(1, escape(title));
    prepared.setString(2, escape(description));
    prepared.setString(3, escape(location));
    Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    prepared.setDate(4, sqlDate, utc);
    // TODO: might want to support event without start time (whole day event)
    if (sqlTime == null) {
      if (sqlDuration == null) {
        prepared.setInt(5, max);
      } else {
        prepared.setObject(5, sqlDuration);
        prepared.setInt(6, max);
      }
    } else {
      if (sqlDuration == null) {
        prepared.setTime(5, sqlTime, utc);
        prepared.setInt(6, max);
      } else {
        prepared.setTime(5, sqlTime, utc);
        prepared.setObject(6, sqlDuration);
        prepared.setInt(7, max);
      }
    }
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

  /**
   * Getters for unit test.
   */
  public int getEventId() {
    return eventId;
  }

  public int getMax() {
    return max;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public String getStartTime() {
    return startTime;
  }

  public String getStartDate() {
    return startDate;
  }

  public String getDuration() {
    return duration;
  }

  /**
   * Used to update response object of put event.
   * 
   * @param eventId
   */
  public void setEventId(int eventId) {
    this.eventId = eventId;
  }

  public void setCurrentCount(String currentCount) {
    if (currentCount != null) {
      this.currentCount = Integer.parseInt(currentCount);
    } else {
      this.currentCount = 0;
    }
  }

  public void setJoined(boolean hasJoined) {
    this.hasJoined = hasJoined;
  }

  public boolean isFound() {
    return found;
  }

  public void setCalendarId(int calendarId) {
    this.calendarId = calendarId;
  }
}
