package req;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import resp.CalendarSubscriptionResponse;
import resp.EventResponse;
import resp.EventSubscriptionResponse;
import sql.SQLQuery;

/**
 * Deserialized JSON object of an API request to create a calendar.
 */
public class CalendarRequest implements Request {

  /**
   * Invalid register request object to replace null checks.
   */
  public static final CalendarRequest INVALID = new CalendarRequest();

  /**
   * Length of time interval (in miliseconds). Events in the specified time
   * interval [startDate, startDate + TIME_INTERVAL] are going to be returned on
   * calendar query.
   */
  public static final int TIME_INTERVAL = 14 * 24 * 60 * 60 * 1000;

  /**
   * Calendar details sent by the client.
   */
  private String name;
  private Integer calendarId;
  private boolean joinEnabled;
  private Timestamp startDate;

  /**
   * Fields excluded from deserialisation.
   */
  private transient String inviteCode;
  private transient int userId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public CalendarRequest() {
  }

  public CalendarRequest(String name, boolean joinEnabled, String inviteCode,
      int userId) {
    this.name = name;
    this.joinEnabled = joinEnabled;
    this.inviteCode = inviteCode;
    this.userId = userId;
  }

  /* Constructor used by GET method of calendar servlet. */
  public CalendarRequest(int calendarId) {
    this.calendarId = calendarId;
  }

  /* Constructor added for testing. */
  public CalendarRequest(Timestamp startDate, int calendarId) {
    this.startDate = startDate;
    this.calendarId = calendarId;
  }

  @Override
  public boolean isValid() {
    return (name != null && !name.isEmpty());
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public void setInviteCode(String inviteCode) {
    this.inviteCode = inviteCode;
  }

  public String getName() {
    return name;
  }

  public boolean isJoinEnabled() {
    return joinEnabled;
  }

  public String getInviteCode() {
    return inviteCode;
  }

  public int getUserId() {
    return userId;
  }

  public Integer getCalendarId() {
    return calendarId;
  }

  public Timestamp getStartDate() {
    return startDate;
  }

  public CalendarEventsQuery getCalendarEventsQuery() {
    return new CalendarEventsQuery();
  }

  public class CalendarEventsQuery implements SQLQuery {
    private ResultSet rs;
    private List<EventResponse> events = new ArrayList<EventResponse>();

    /*
     * WITH x AS (SELECT "EID", COUNT(*) FROM "EVENT_USER" GROUP BY "EID")
     * SELECT "EVENT"."EID", "TITLE", "DESCRIPTION", "LOCATION", "DATE", "TIME",
     * "DURATION", "count", "MAX_ATTENDEES", EXISTS (SELECT "UID" FROM
     * "EVENT_USER" WHERE "UID"=76 AND "EID"="EVENT"."EID") FROM x RIGHT OUTER
     * JOIN "EVENT" ON x."EID" = "EVENT"."EID" WHERE ("DATE" + "TIME", "DATE" +
     * "TIME" + "DURATION") OVERLAPS ('1000-03-03', '3000-03-03') AND
     * "EVENT"."EID" IN (SELECT "EID" FROM "CALENDAR_EVENT" WHERE "CID"=31);
     */

    public String getSQLQuery() {
      Timestamp endDate = new Timestamp(startDate.getTime() + TIME_INTERVAL);
      return String
          .format(
              "WITH x AS (SELECT \"%s\", COUNT(*) FROM \"EVENT_USER\" GROUP BY \"%s\")"
                  + "SELECT  \"EVENT\".\"EID\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"count\", \"%s\", EXISTS (SELECT \"%s\" FROM \"EVENT_USER\" WHERE \"%s\"=%s AND \"%s\"=\"EVENT\".\"%s\")"
                  + "FROM x RIGHT OUTER JOIN \"EVENT\" ON x.\"%s\" = \"EVENT\".\"%s\""
                  + "WHERE (\"DATE\" + \"TIME\", \"DATE\" + \"TIME\" + \"DURATION\") "
                  + "OVERLAPS ('%s', '%s') "
                  + "AND "
                  + "\"EVENT\".\"EID\" IN (SELECT \"EID\" FROM \"CALENDAR_EVENT\" WHERE \"CID\"=%d);",
              EventSubscriptionResponse.EID_COLUMN,
              EventSubscriptionResponse.EID_COLUMN, EventResponse.TITLE_COLUMN,
              EventResponse.DESC_COLUMN, EventResponse.LOCATION_COLUMN,
              EventResponse.DATE_COLUMN, EventResponse.TIME_COLUMN,
              EventResponse.DURATION_COLUMN, EventResponse.MAX_ATTEDEE_COLUMN,
              EventSubscriptionResponse.EID_COLUMN,
              EventSubscriptionResponse.UID_COLUMN, userId,
              EventSubscriptionResponse.EID_COLUMN, EventResponse.EID_COLUMN,
              EventSubscriptionResponse.EID_COLUMN, EventResponse.EID_COLUMN,
              startDate, endDate, calendarId);
    }

    @Override
    public void setResult(ResultSet result) {
      this.rs = result;
      try {
        while (rs.next()) {
          EventResponse resp = new EventResponse(
              rs.getString(EventResponse.TITLE_COLUMN),
              rs.getString(EventResponse.DESC_COLUMN),
              rs.getString(EventResponse.LOCATION_COLUMN),
              rs.getString(EventResponse.TIME_COLUMN),
              rs.getString(EventResponse.DATE_COLUMN),
              rs.getString(EventResponse.DURATION_COLUMN),
              rs.getString(EventResponse.MAX_ATTEDEE_COLUMN),
              rs.getInt(EventResponse.EID_COLUMN), calendarId);
          resp.setCurrentCount(rs.getString("count"));
          events.add(resp);
        }
      } catch (SQLException e) {
        System.err.println("Error getting the result while creating "
            + "calendarResponse object.");
        return;
      }
    }

    public List<EventResponse> getEvents() {
      return events;
    }
  }
}
