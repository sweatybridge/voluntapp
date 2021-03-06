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
        "SELECT \"EVENT\".\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\" " +
        "FROM \"EVENT\" JOIN \"SAVED_EVENT\" ON  " +
        "\"EVENT\".\"EID\" = \"SAVED_EVENT\".\"EID\" WHERE \"%s\"=?;",
        EventResponse.EID_COLUMN, EventResponse.TITLE_COLUMN, 
        EventResponse.DESC_COLUMN, EventResponse.LOCATION_COLUMN, 
        EventResponse.DATE_COLUMN, EventResponse.TIME_COLUMN, 
        EventResponse.DURATION_COLUMN, EventResponse.MAX_ATTEDEE_COLUMN, 
        TIMESTAMP_COLUMN, EventResponse.ACTIVE_COLUMN, UID_COLUMN);
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
        tempEventList.add(
            new Pair<Timestamp, EventResponse>(result.getTimestamp(TIMESTAMP_COLUMN), event));
      } while(event.isFound());
      // Sort the saved events in reverse-chronological order
      Collections.sort(tempEventList);
      ListIterator<Pair<Timestamp, EventResponse>> iter = 
          tempEventList.listIterator(tempEventList.size());
      while(iter.hasPrevious()) {
        savedEvents.add(iter.previous().getValue()); 
      }
    } catch (SQLException e) {
      System.err.println("Error while getting the list of saved events.");
      e.printStackTrace();
    }
  }

  public String getSQLInsert() {
    return String.format(
        "INSERT INTO \"SAVED_EVENT\" (\"%s\", \"%s\") SELECT ?, ? " +
        "WHERE NOT EXISTS (SELECT 1 FROM \"SAVED_EVENT\" WHERE \"%s\"=? AND \"%s\"=?); ",
        UID_COLUMN, EID_COLUMN, UID_COLUMN, EID_COLUMN);
  }

  
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {
    prepared.setInt(1, userId);
    prepared.setInt(2, eventId);
    prepared.setInt(3, userId);
    prepared.setInt(4, eventId);
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
  
  public String getSQLUpdate() {
    return String.format("UPDATE \"SAVED_EVENT\" SET \"%s\"=now() " +
    		"WHERE \"%s\"=? AND \"%s\"=?;", TIMESTAMP_COLUMN, UID_COLUMN, EID_COLUMN);
  }

  public void formatSQLUpdate(PreparedStatement prepare) throws SQLException {
    prepare.setInt(1, userId);
    prepare.setInt(2, eventId);
  }
}
