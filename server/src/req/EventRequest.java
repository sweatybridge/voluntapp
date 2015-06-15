package req;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.validator.routines.CalendarValidator;

import utils.EventStatus;

import com.google.common.annotations.VisibleForTesting;

/**
 * Deserialized JSON object of an API request to create new event.
 */
public class EventRequest implements Request {

  private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("UTC");

  /**
   * Event details sent by the client. No primitive types so that gson will
   * deserialise partial updates to null.
   */
  private Integer eventId;
  private Integer calendarId;
  private String title;
  private String description;
  private String location;
  private String startTime; // HH:mm
  private String startDate; // yyyy-MM-dd
  private String endTime;
  private String endDate;
  private String timezone;
  private Integer max;
  private EventStatus status; // Field used to perform updates of event state.
  private String startDateTime;
  private String endDateTime;

  /**
   * Fields excluded from deserialisation.
   */
  private transient Calendar calStartDateTime;
  private transient Calendar calEndDateTime;
  private transient TimeZone clientTimezone;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public EventRequest() {}

  @VisibleForTesting
  protected EventRequest(String title, String description, String location,
      String startTime, String startDate, String endTime, String endDate,
      String timezone, int max, int calendarId) {
    this.title = title;
    this.description = description;
    this.location = location;
    this.startTime = startTime;
    this.startDate = startDate;
    this.endTime = endTime;
    this.endDate = endDate;
    this.timezone = timezone;
    this.max = max;
    this.calendarId = calendarId;
  }

  @VisibleForTesting
  public EventRequest(String title, String description, String location,
      Calendar startDateTime, Calendar endDateTime, TimeZone clientTimezone,
      int max, int calendarId) {
    this.title = title;
    this.description = description;
    this.location = location;
    this.calStartDateTime = startDateTime;
    this.calEndDateTime = endDateTime;
    this.clientTimezone = clientTimezone;
    this.max = max;
    this.calendarId = calendarId;

    if (startDateTime != null) {
      Date start = startDateTime.getTime();
      SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
      df.setTimeZone(clientTimezone);
      this.startDate = df.format(start);
      SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
      tf.setTimeZone(clientTimezone);
      this.startTime = tf.format(start);
    }
  }

  @Override
  public boolean isValid() {
    return (title != null && !title.isEmpty()) && (max >= -1)
        && (calendarId > 0) && isDateTimeValid();
  }

  public boolean isDateTimeValid() {
    calStartDateTime = CalendarValidator.getInstance().validate(startDateTime, DATE_TIME_PATTERN, DEFAULT_TIMEZONE);
    calEndDateTime = CalendarValidator.getInstance().validate(endDateTime, DATE_TIME_PATTERN, DEFAULT_TIMEZONE);
    return calStartDateTime != null && calEndDateTime != null;
  }

  public static void main(String[] args) {
    Calendar cal =
        CalendarValidator.getInstance().validate("2015-06-16T13:00:00Z",
            "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"));
    System.out.println(cal.getTime());
  }

  public boolean isPartiallyValid() {
    return eventId == null
        && calendarId == null
        && (title == null || !title.isEmpty())
        && (max == null || max >= -1)
        && (description == null || !description.isEmpty())
        && (location == null || !location.isEmpty())
        && isDateTimeValid();
  }

  private boolean isTimezoneValid() {
    clientTimezone = TimeZone.getTimeZone(timezone);
    return clientTimezone != null;
  }

  private boolean isStartDateTimeValid() {
    if (startDate == null || startDate.isEmpty() || startTime == null
        || startTime.isEmpty()) {
      return false;
    }
    calStartDateTime =
        CalendarValidator.getInstance().validate(startDate + startTime,
            DATE_TIME_PATTERN, clientTimezone);
    return calStartDateTime != null;
  }

  private boolean isEndDateTimeValid() {
    if (endDate == null || endDate.isEmpty() || endTime == null
        || endTime.isEmpty()) {
      return true;
    }
    calEndDateTime =
        CalendarValidator.getInstance().validate(endDate + endTime,
            DATE_TIME_PATTERN, clientTimezone);
    return calEndDateTime != null && calEndDateTime.after(calStartDateTime);
  }

  public int getCalendarId() {
    return calendarId;
  }

  public String getTitle() {
    return title;
  }

  public Calendar getStartDateTime() {
    return calStartDateTime;
  }

  public TimeZone getClientTimezone() {
    return clientTimezone;
  }

  public Integer getMax() {
    return max;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public Calendar getEndDateTime() {
    return calEndDateTime;
  }

  public void setEventId(int eventId) {
    this.eventId = eventId;
  }

  public void setCalendarId(int calendarId) {
    this.calendarId = calendarId;
  }
  
  public void setEventStatus(EventStatus status) {
    this.status = status;
  }
  
  public EventStatus getStatus() {
    return status;
  }
}
