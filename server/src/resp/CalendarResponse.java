package resp;

import java.sql.Timestamp;
import java.util.List;

import sql.SQLInsert;

/**
 * A successful response to a calendar request.
 */
public class CalendarResponse extends Response implements SQLInsert {
  
  public static String CID_COLUMN = "ID";
  /**
   * Calendar details returned to client.
   */
  private int calendarId;
  private String name;
  private boolean joinEnabled;
  private String joinCode;
  /**
   * Fields excluded from deserialisation.
   */
  private transient Timestamp creationDate;
  private transient int userId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public CalendarResponse() {}

  /**
   */
  public CalendarResponse(String name, boolean joinEnabled, int userId, 
      String joinCode) {
    this.name = name;
    this.joinEnabled = joinEnabled;
    this.userId = userId;
    this.joinCode = joinCode;
  }

  public int getCalendarId() {
    return calendarId;
  }

  @Override
  public String getSQLInsert() {
    return String.format("INSERT INTO public.\"CALENDAR\" VALUES " +
    		"(DEFAULT, '%s', '%d', DEFAULT, %b, '%s');", name, userId, joinEnabled, 
    		joinCode);
  }

  public void setCalendarID(int id) {
    this.calendarId = id;
  }
}
