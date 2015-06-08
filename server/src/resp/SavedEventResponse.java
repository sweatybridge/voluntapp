package resp;

import java.awt.Event;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import sql.SQLDelete;

public class SavedEventResponse extends Response {
  
  public static String EID_COLUMN = "EID";
  public static String UID_COLUMN = "UID";
  public static String TIMESTAMP_COLUMN = "TIMESTAMP";
  
  /**
   * SavedEvents returned to the client.
   */
  private List<SavedEvent> savedEvents = new ArrayList<SavedEvent>();
  
  /**
   * Fields excluded from serialisation.
   */
  private transient int userId;
  private transient int eventId;
  
  
  /**
   * Inner class used to represent the saved event (the event and the timestamp).
   */
  private class SavedEvent {
    private EventResponse event;
    private Timestamp timestamp;
    
    /**
     * No-arg constructor for compatibility with gson serialiser.
     */
    public SavedEvent() {}
    
    public SavedEvent(EventResponse event, Timestamp timestamp) {
      this.event = event;
      this.timestamp = timestamp;
    }
  }
  
  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public SavedEventResponse() {}
  
  public SavedEventResponse(int userId) {
    this.userId = userId;
  }
  
  public SavedEventResponse(int userId, int eventId) {
    this.userId = userId;
    this.eventId = eventId;
  }

  @Override
  public String getSQLQuery() {
    return String.format(
        "SELECT \"EVENT\".\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\" " +
        "FROM \"EVENT\" JOIN \"SAVED_EVENT\" ON  " +
        "\"EVENT\".\"EID\" = \"SAVED_EVENT\".\"EID\" WHERE \"%s\"=?;",
        EventResponse.EID_COLUMN, EventResponse.TITLE_COLUMN, 
        EventResponse.DESC_COLUMN, EventResponse.LOCATION_COLUMN, 
        EventResponse.DATE_COLUMN, EventResponse.TIME_COLUMN, 
        EventResponse.DURATION_COLUMN, EventResponse.MAX_ATTEDEE_COLUMN, 
        TIMESTAMP_COLUMN, UID_COLUMN);
  }
  
  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    prepared.setInt(1, userId);
  }
  
  @Override
  public void setResult(ResultSet result) {
    try {
      EventResponse event;
      do {
        event = new EventResponse();
        event.setResult(result);   
        savedEvents.add(
            new SavedEvent(event, result.getTimestamp(TIMESTAMP_COLUMN)));
      } while(event.isFound());
    } catch (SQLException e) {
      System.err.println("Error while getting the list of saved events.");
      e.printStackTrace();
    }
  }

  public String getSQLInsert() {
    return String.format("INSERT INTO \"SAVED_EVENT\" (\"%s\", \"%s\") " +
    		"VALUES (?, ?);", UID_COLUMN, EID_COLUMN);
  }

  
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {
    prepared.setInt(1, userId);
    prepared.setInt(2, eventId);
  }
  
  public String getSQLDelete() {
    return String.format("DELETE FROM \"SAVED_EVENT\" WHERE \"%s\"=? AND \"%s\"=?;", 
        UID_COLUMN, EID_COLUMN);  
  }
  
  public void formatSQLDelete(PreparedStatement prepared) throws SQLException {
    prepared.setInt(1, userId);
    prepared.setInt(2, eventId);
  }
  
  public static void main(String[] args) {
    SavedEventResponse event = new SavedEventResponse(183);
    System.out.println(event.getSQLInsert());
  }
}
