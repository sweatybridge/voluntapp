package resp;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;

import org.postgresql.util.PGInterval;

import req.EventRequest;

import utils.EventStatus;

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
  public static String CREATOR_COLUMN = "CREATOR";

  /**
   * Event details returned to the client. Always in UTC.
   */
  private int eventId = -1;
  private int calendarId = -1;
  private String title;
  private String description;
  private String location;
  private String duration; // HH:mm
  private String startDateTime;
  private String endDateTime;
  private Integer currentCount;
  private Integer max;
  private Boolean hasJoined;
  private Set<UserResponse> volunteers;
  private EventStatus status;
  private int userId;

  /**
   * Other variables used by the database interface
   */
  private transient ResultSet rs;
  private transient PGInterval sqlDuration;
  private transient Date sqlDate;
  private transient Time sqlTime;
  private transient boolean found;
  private transient String startTime; // HH:mm
  private transient String startDate; // YYYY-MM-DD
  private transient boolean isPrivileged = false;

  /**
   * Fields excluded from serialisation.
   */

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
      Calendar startDateTime, Calendar endDateTime, Integer max, int eventId,
      int calendarId, EventStatus status, int userId) {
    this.title = title;
    this.description = description;
    this.location = location;
    this.eventId = eventId;
    this.calendarId = calendarId;
    this.userId = userId;
    this.max = max;
    this.status = status;
    this.currentCount = 0;

    // Parse calendar to sql date time and pginterval for storage
    if (startDateTime != null) {
      long start = startDateTime.getTimeInMillis();
      this.sqlDate = new Date(start);
      this.sqlTime = new Time(start);
      this.sqlDuration = new PGInterval(
          0,
          0,
          endDateTime.get(Calendar.DATE) - startDateTime.get(Calendar.DATE),
          endDateTime.get(Calendar.HOUR_OF_DAY)
              - startDateTime.get(Calendar.HOUR_OF_DAY),
          endDateTime.get(Calendar.MINUTE) - startDateTime.get(Calendar.MINUTE),
          0);
      
      SimpleDateFormat sdf = new SimpleDateFormat(EventRequest.UTC_PATTERN);
      sdf.setTimeZone(EventRequest.UTC_TIMEZONE);
      this.startDateTime = sdf.format(startDateTime.getTime());
      this.endDateTime = sdf.format(endDateTime.getTime());
    }
  }

  /**
   * Constructor for deleting event.
   * 
   * @param eventId
   * @param status 
   */
  public EventResponse(int eventId, EventStatus status) {
    this.eventId = eventId;
    this.status = status;
  }

  /**
   * Constructor used for getting a single event by id.
   * 
   * @param eid
   */
  public EventResponse(int eventId) {
    this.eventId = eventId;
  }

  public void setPriviledge() {
    isPrivileged = true;
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
    this.status = EventStatus.translateToEnum(rs.getString(ACTIVE_COLUMN));

    // Fill in composite fields
    SimpleDateFormat sdf = new SimpleDateFormat(EventRequest.UTC_PATTERN);
    // sdf.setTimeZone(EventRequest.UTC_TIMEZONE);
    java.util.Date startDate =
        new java.util.Date(sqlDate.getTime() + sqlTime.getTime());
    this.startDateTime = sdf.format(startDate);
    sqlDuration.add(startDate);
    this.endDateTime = sdf.format(startDate);
  }

  @Override
  public String getSQLUpdate() {
    int found = 0;
    String formatString = ((title == null || found++ == Integer.MIN_VALUE) ? ""
        : String.format("\"%s\"=?,", TITLE_COLUMN))
        + ((description == null || found++ == Integer.MIN_VALUE) ? "" : String
            .format("\"%s\"=?,", DESC_COLUMN))
        + ((location == null || found++ == Integer.MIN_VALUE) ? "" : String
            .format("\"%s\"=?,", LOCATION_COLUMN))
        + ((sqlDate == null || found++ == Integer.MIN_VALUE) ? "" : String
            .format("\"%s\"=?,", DATE_COLUMN))
        + ((sqlTime == null || found++ == Integer.MIN_VALUE) ? "" : String
            .format("\"%s\"=?,", TIME_COLUMN))
        + ((sqlDuration == null || found++ == Integer.MIN_VALUE) ? "" : String
            .format("\"%s\"=?,", DURATION_COLUMN))
        + ((max == null || found++ == Integer.MIN_VALUE) ? "" : String.format(
            "\"%s\"=?,", MAX_ATTEDEE_COLUMN))
        + ((status == null || found++ == Integer.MIN_VALUE) ? "" : String.format(
            "\"%s\"='%s'::\"%s\",", ACTIVE_COLUMN, status.getName(),
            EventStatus.STATUS_ENUM_NAME));
    return (found == 0) ? null : String.format(
        "UPDATE public.\"EVENT\" SET %s WHERE \"EID\"=?",
        formatString.substring(0, formatString.length() - 1));
  }

  @Override
  public void formatSQLUpdate(PreparedStatement prepared) throws SQLException {
    int i = 1;
    if (title != null)
      prepared.setString(i++, escape(title));
    if (description != null)
      prepared.setString(i++, escape(description));
    if (location != null)
      prepared.setString(i++, escape(location));
    if (sqlDate != null)
      prepared.setDate(i++, sqlDate);
    if (sqlTime != null)
      prepared.setTime(i++, sqlTime);
    if (sqlDuration != null)
      prepared.setObject(i++, sqlDuration);
    if (max != null)
      prepared.setInt(i++, max);
    prepared.setInt(i++, eventId);
  }

  @Override
  public void checkResult(int rowsAffected) {
    // TODO Auto-generated method stub
  }

  @Override
  public String getSQLInsert() {
    return String
        .format(
            "WITH x AS (INSERT INTO \"EVENT\" VALUES (DEFAULT, ?, ?, ?, ?, %s, %s, ?, DEFAULT, DEFAULT, DEFAULT, '%s'::\"%s\", %d) RETURNING \"EID\") INSERT INTO \"CALENDAR_EVENT\"  SELECT %d,\"EID\" FROM x;",
            (sqlTime == null) ? "DEFAULT" : "?",
            (sqlDuration == null) ? "DEFAULT" : "?",
            status.getName(), EventStatus.STATUS_ENUM_NAME, userId, calendarId);
  }

  @Override
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {
    Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    int i = 1;
    prepared.setString(i++, escape(title));
    prepared.setString(i++, escape(description));
    prepared.setString(i++, escape(location));
    prepared.setDate(i++, sqlDate, utc);
    if (sqlTime != null)
      prepared.setTime(i++, sqlTime, utc);
    if (sqlDuration != null)
      prepared.setObject(i++, sqlDuration);
    prepared.setInt(i++, max);
  }

  @Override
  public String getSQLQuery() {
    return String.format("SELECT * FROM \"EVENT\" WHERE \"%s\"=? AND \"%s\"!='%s' %s;",
        EID_COLUMN, ACTIVE_COLUMN, EventStatus.DELETED.getName(), 
        (isPrivileged) ? "" : "AND \"ACTIVE\"='active'");
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    prepared.setInt(1, eventId);
  };

  @Override
  public void setResult(ResultSet result) {
    this.rs = result;
    try {
      found = rs.next();
      if (found) {
        setEventResponse();
      }
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
  
  public EventStatus getStatus() {
    return status;
  }
}
