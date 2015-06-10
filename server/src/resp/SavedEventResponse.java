package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import utils.Pair;

public class SavedEventResponse extends Response {
  
  public static String EID_COLUMN = "EID";
  public static String UID_COLUMN = "UID";
  public static String TIMESTAMP_COLUMN = "TIMESTAMP";
  
  /**
   * SavedEvents returned to the client.
   */
  private List<EventResponse> savedEvents = new ArrayList<EventResponse>();
  
  /**
   * Fields excluded from serialisation.
   */
  private transient int userId;
  private transient int eventId;
  private transient List<Pair<Timestamp, EventResponse>> tempEventList = 
      new ArrayList<Pair<Timestamp, EventResponse>>();
  
  
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
        if (!event.isFound()) break;
        if (event.isFound()) System.out.println(event.getEventId());
        tempEventList.add(
            new Pair<Timestamp, EventResponse>(result.getTimestamp(TIMESTAMP_COLUMN), event));
      } while(event.isFound());
      // Sort the saved events in reverse-chronological order
      Collections.sort(tempEventList);
      ListIterator<Pair<Timestamp, EventResponse>> iter = 
          tempEventList.listIterator(tempEventList.size());
      /*for (Pair<Timestamp, EventResponse> pair : tempEventList) {
        savedEvents.add(pair.getValue()); 
      }
      while(iter.hasPrevious()) {
        System.out.println("TEST");
        savedEvents.add(iter.previous().getValue()); 
      }*/
      for (int i=tempEventList.size() - 1; i>0; i--) {
        savedEvents.add(tempEventList.get(i).getValue());
      }
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
