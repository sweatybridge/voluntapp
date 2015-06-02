package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import utils.AuthLevel;

/**
 * A successful response to a calendar request.
 */
public class CalendarResponse extends Response {

  public static final CalendarResponse NO_CALENDAR = new CalendarResponse();

  public static String CID_COLUMN = "ID";
  public static String CNAME_COLUMN = "NAME";
  public static String CREATOR_COLUMN = "CREATOR";
  public static String CREATED_COLUMN = "CREATED";
  public static String JOIN_ENABLED_COLUMN = "JOIN_ENABLED";
  public static String JOIN_CODE_COLUMN = "JOIN_CODE";
  public static String ACTIVE_COLUMN = "ACTIVE";
  public static String ROLE_COLUMN = "ROLE";

  /**
   * Calendar details returned to client.
   */
  private int calendarId;
  private String name;
  private Boolean joinEnabled;
  private String joinCode;
  private String role = "none";
  private List<EventResponse> events;
  /**
   * Fields excluded from deserialisation.
   */
  private transient ResultSet rs;
  private transient Timestamp creationDate;
  private transient int userId;
  private transient Boolean active;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public CalendarResponse() {
  }

  /* Constructor used for querying calendar information. */
  public CalendarResponse(int calendarId) {
    this.calendarId = calendarId;
  }

  /* Constructor used for deleting calendars from the database. */
  public CalendarResponse(int calendarId, Boolean active) {
    this.calendarId = calendarId;
    this.active = active;
  }

  /* Constructor used for updating calendar data (name and join enabled). */
  public CalendarResponse(int calendarId, String name, Boolean joinEnabled) {
    this.calendarId = calendarId;
    this.name = name;
    this.joinEnabled = joinEnabled;
  }

  public CalendarResponse(String name, boolean joinEnabled, int userId,
      String joinCode) {
    this.name = name;
    this.joinEnabled = joinEnabled;
    this.userId = userId;
    this.joinCode = joinCode;
  }

  public CalendarResponse(int cid, int uid) {
    this.calendarId = cid;
    this.userId = uid;
  }

  public int getCalendarId() {
    return calendarId;
  }

  @Override
  public String getSQLInsert() {
    return String
        .format("INSERT INTO \"CALENDAR\" VALUES (DEFAULT, ?, ?, DEFAULT, ?, ?, DEFAULT);");
  }

  @Override
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {
    prepared.setString(1, escape(name));
    prepared.setInt(2, userId);
    prepared.setBoolean(3, joinEnabled);
    prepared.setString(4, escape(joinCode));
  }
  
  
   /* 
   * SELECT * FROM \"CALENDAR\" JOIN (SELECT \"CID\",\"ROLE\" FROM
   * \"USER_CALENDAR\" WHERE \"UID\"=%d) AS x ON \"CALENDAR\".\"ID\" =x.\"CID\"
   * WHERE \"CID\" =%d AND \"ACTIVE\"=true;
   */

  @Override
  public String getSQLQuery() {
    return String
        .format(
            "SELECT * FROM \"CALENDAR\" JOIN (SELECT \"%s\",\"%s\" FROM \"USER_CALENDAR\" WHERE \"%s\"=?) AS x ON \"CALENDAR\".\"%s\" =x.\"%s\" WHERE \"%s\" = ? AND \"%s\"=true;",
            CalendarSubscriptionResponse.CID_COLUMN,
            CalendarSubscriptionResponse.ROLE_COLUMN,
            CalendarSubscriptionResponse.UID_COLUMN, CID_COLUMN,
            CalendarSubscriptionResponse.CID_COLUMN,
            CalendarSubscriptionResponse.CID_COLUMN, ACTIVE_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepare) throws SQLException {
    prepare.setInt(1, userId);
    prepare.setInt(2, calendarId);
  }

  @Override
  public String getSQLUpdate() {
    StringBuilder setUpdate = new StringBuilder();
    setUpdate.append(name != null ? String.format("\"%s\"=?,", CNAME_COLUMN)
        : "");
    setUpdate.append(joinEnabled != null ? String.format("\"%s\"=?,",
        JOIN_ENABLED_COLUMN) : "");
    setUpdate.append(active != null ? String.format("\"%s\"=?,", ACTIVE_COLUMN)
        : "");

    String update = setUpdate.toString();
    if (update.isEmpty()) {
      return null;
    }
    update = update.substring(0, update.length() - 1);
    return String.format("UPDATE \"CALENDAR\" SET " + update
        + " WHERE \"%s\"=?;", CID_COLUMN);
  }

  @Override
  public void formatSQLUpdate(PreparedStatement prepare) throws SQLException {
    int i = 1;
    if (name != null)
      prepare.setString(i++, escape(name));
    if (joinEnabled != null)
      prepare.setBoolean(i++, joinEnabled);
    if (active != null)
      prepare.setBoolean(i++, active);
    prepare.setInt(i++, calendarId);
  }

  public List<EventResponse> getCalendarEvents() {
    return events;
  }

  @Override
  public void setResult(ResultSet result) {
    this.rs = result;
    System.out.println("HERE");
    try {
      System.out.println(rs.getWarnings());
      rs.next();
      setCalendarResponse();
    } catch (SQLException e) {
      System.err.println("Error getting the result while creating "
          + "calendarResponse object.");
      return;
    }
  }

  private void setCalendarResponse() throws SQLException {
    name = rs.getString(CNAME_COLUMN);
    userId = rs.getInt(CREATOR_COLUMN);
    creationDate = rs.getTimestamp(CREATED_COLUMN);
    joinEnabled = rs.getBoolean(JOIN_ENABLED_COLUMN);
    joinCode = rs.getString(JOIN_CODE_COLUMN);
    active = rs.getBoolean(ACTIVE_COLUMN);
    role = rs.getString(ROLE_COLUMN);
  }

  public void setCalendarID(int id) {
    this.calendarId = id;
  }

  public void setEvents(List<EventResponse> events) {
    this.events = events;
  }

  public void addEvent(EventResponse event) {
    events.add(event);
  }

  public AuthLevel getRole() {
    return AuthLevel.getAuth(role);
  }

  public void setJoinCode(String joinCode) {
    this.joinCode = joinCode;
  }

  public void setJoinEnabled(Boolean joinEnabled) {
    this.joinEnabled = joinEnabled;
  }
}
