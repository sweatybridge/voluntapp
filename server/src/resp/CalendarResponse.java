package resp;

import java.util.List;

/**
 * A successful response to a calendar request.
 */
public class CalendarResponse extends Response {

  /**
   * Calendar details returned to client.
   */
  private int calendarId;
  private String name;
  private boolean joinEnabled;
  private String inviteCode;
  private List<EventResponse> events;

  /**
   * Fields excluded from deserialisation.
   */
  private transient int userId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public CalendarResponse() {}

  /**
   */
  public CalendarResponse(String name, boolean joinEnabled, String inviteCode,
      List<EventResponse> events) {
    this.name = name;
    this.joinEnabled = joinEnabled;
    this.inviteCode = inviteCode;
    this.events = events;
  }

  public int getCalendarId() {
    return calendarId;
  }
}
