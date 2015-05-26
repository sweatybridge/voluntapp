package req;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.validator.routines.CalendarValidator;

/**
 * Deserialized JSON object of an API request to create new event.
 */
public class EventRequest implements Request {

  private static final String DATE_TIME_PATTERN = "yyyy-MM-ddHH:mm";

  /**
   * Event details sent by the client.
   */
  private int calendarId;
  private String title;
  private String description;
  private String location;
  private String startTime; // HH:mm
  private String startDate; // yyyy-MM-dd
  private String endTime;
  private String endDate;
  private String timezone;
  private int max;

  /**
   * Fields excluded from deserialisation.
   */
  private transient Calendar startDateTime;
  private transient Calendar endDateTime;
  private transient TimeZone clientTimezone;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public EventRequest() {}

  public EventRequest(String title, String description, String location,
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

  @Override
  public boolean isValid() {
    return (title != null && !title.isEmpty()) && (max >= 0)
        && (calendarId > 0) && isTimezoneValid() && isStartDateTimeValid()
        && isEndDateTimeValid();
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
    startDateTime =
        CalendarValidator.getInstance().validate(startDate + startTime,
            DATE_TIME_PATTERN, clientTimezone);
    return startDateTime != null;
  }

  private boolean isEndDateTimeValid() {
    if (endDate == null || endDate.isEmpty() || endTime == null
        || endTime.isEmpty()) {
      return true;
    }
    endDateTime =
        CalendarValidator.getInstance().validate(endDate + endTime,
            DATE_TIME_PATTERN, clientTimezone);
    return endDateTime != null;
  }

  public int getCalendarId() {
    return calendarId;
  }

  public String getTitle() {
    return title;
  }

  public Calendar getStartDateTime() {
    return startDateTime;
  }

  public TimeZone getClientTimezone() {
    return clientTimezone;
  }

  public int getMax() {
    return max;
  }

  /**
   * @return could be null
   */
  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public Calendar getEndDateTime() {
    return endDateTime;
  }

}
