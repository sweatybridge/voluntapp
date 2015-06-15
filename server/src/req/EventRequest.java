package req;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.validator.routines.CalendarValidator;

import utils.EventStatus;

import com.google.common.annotations.VisibleForTesting;

/**
 * Deserialized JSON object of an API request to create new event.
 */
public class EventRequest implements Request {

  public static final String UTC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");

  /**
   * Event details sent by the client. No primitive types so that gson will
   * deserialise partial updates to null.
   */
  private Integer eventId;
  private Integer calendarId;
  private String title;
  private String description;
  private String location;
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
  }

  @Override
  public boolean isValid() {
    return (title != null && !title.isEmpty()) && (max >= -1)
        && (calendarId > 0) && isDateTimeValid();
  }

  public boolean isDateTimeValid() {
    calStartDateTime =
        CalendarValidator.getInstance().validate(startDateTime, UTC_PATTERN,
            UTC_TIMEZONE);
    calEndDateTime =
        CalendarValidator.getInstance().validate(endDateTime, UTC_PATTERN,
            UTC_TIMEZONE);
    return calStartDateTime != null && calEndDateTime != null;
  }

  public boolean isPartiallyValid() {
    return eventId == null && calendarId == null
        && (title == null || !title.isEmpty()) && (max == null || max >= -1)
        && (description == null || !description.isEmpty())
        && (location == null || !location.isEmpty()) && isDateTimeValid();
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
