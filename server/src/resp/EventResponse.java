package resp;

/**
 * A successful response to a event request.
 */
public class EventResponse extends Response {

  /**
   * Event details returned to the client.
   */
  private int eventId;
  private String title;
  private String description;
  private String location;
  private String time; // HH:MM
  private String date; // YY-MM-DD

  /**
   * Fields excluded from serialisation.
   */
  private transient int calendarId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public EventResponse() {}

  /**
   * Constructs a successful event response.
   * 
   * @param email Email of the user response
   * @param hashedPassword Password found in the database
   * @param userId The ID of the user requests
   */
  public EventResponse(String title, String description, String location,
      String time, String date, int calendarId) {
    this.title = title;
    this.description = description;
    this.location = location;
    this.time = time;
    this.date = date;
    this.calendarId = calendarId;
  }

  public int getCalendarId() {
    return calendarId;
  }
}
