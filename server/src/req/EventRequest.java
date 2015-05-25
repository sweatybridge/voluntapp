package req;

/**
 * Deserialized JSON object of an API request to create new event.
 */
public class EventRequest implements Request {

  private static final String TIME_PATTERN =
      "^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$";
  private static final String DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";

  /**
   * Event details sent by the client.
   */
  private int calendarId;
  private String title;
  private String description;
  private String location;
  private String time; // HH:MM
  private String date; // YYYY-MM-DD
  private String duration;
  private String max;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public EventRequest() {}

  public EventRequest(String title, String description, String location,
      String time, String date, String duration, String max, int calendarId) {
    this.title = title;
    this.description = description;
    this.location = location;
    this.time = time;
    this.date = date;
    this.duration = duration;
    this.calendarId = calendarId;
    this.max = max;
  }

  @Override
  public boolean isValid() {
    return (title != null && !title.isEmpty())
        && (description != null && !description.isEmpty())
        && (location != null && !location.isEmpty())
        && (time != null && time.matches(TIME_PATTERN))
        && (date != null && date.matches(DATE_PATTERN));
  }

  public int getCalendarId() {
    return calendarId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }
  
  public String getMax() {
    return max;
  }

  public String getLocation() {
    return location;
  }

  public String getTime() {
    return time;
  }

  public String getDate() {
    return date;
  }
  
  public String getDuration() {
    return duration;
  }
}
