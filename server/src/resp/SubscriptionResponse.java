package resp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sql.SQLQuery;

public class SubscriptionResponse extends Response implements SQLQuery {
  
  /* Columns of the USER_CALENDAR table. */
  private static final String UID_COLUMN = "UID";
  private static final String CID_COLUMN = "CID";
  
  private List<Integer> calendarIds = new ArrayList<Integer>();
  
  /**
   * Fields excluded from serialisation.
   */
  private transient int userId;
  private transient ResultSet rs;
  
  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public SubscriptionResponse() {}
  
  public SubscriptionResponse(int userId) {
    this.userId = userId;
  }
  
  @Override
  public String getSQLQuery() {
    return String.format("" +
    		"SELECT \"%s\" FROM \"USER_CALENDAR\" WHERE \"%s\"=%d;", 
    		CID_COLUMN, UID_COLUMN, userId);
  }

  @Override
  public void setResult(ResultSet result) {
    rs = result;
    try {
      while(rs.next()) {
        calendarIds.add(rs.getInt(CID_COLUMN));
      }
    } catch (SQLException e){
      System.err.println(
          "Error while reading the results of USER_ID - CALENDAR_ID query.");
    }
  }
  
  public List<Integer> getCalendarIds() {
    return calendarIds;
  }
  
  public void setCalendarIds(List<Integer> calendarIds) {
    this.calendarIds = calendarIds;
  }
}
