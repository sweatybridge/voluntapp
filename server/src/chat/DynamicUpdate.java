package chat;

import java.util.Arrays;
import java.util.Date;

import db.CalendarIdUserIdMap;
import resp.CalendarResponse;
import resp.EventResponse;

/**
 * Sends dynamic updates to the calendar subscribers through the chat server. It
 * uses the CalendarIdUserIdMap to find which users should be notified.
 * 
 * @author nc1813
 * @author pc3813
 * 
 */
public class DynamicUpdate {

  /**
   * Given the calendarId, it sends the obj to all of its ONLINE subscribers
   * given an MessageType
   * 
   * @param calendarId
   *          CalendarId of which the ONLINE subscribers will be notified
   * @param mType
   *          The type of the ChatMessage
   * @param obj
   *          The object that is going to be sent
   */
  private static void sendObj(Integer calendarId, MessageType mType, Object obj) {
    CalendarIdUserIdMap map = CalendarIdUserIdMap.getInstance();
    ChatMessage cm = new ChatMessage(mType.getType(), Arrays.asList(map
        .getUserIds(calendarId)), -1, new Date(), false, obj);
    ChatServer.routeChatMessage(cm);
  }

  /**
   * Sends an "update/event" to subscribers to the calendar
   * 
   * @param calendarId
   *          CalendarId of which the ONLINE subscribers will be notified
   * @param event
   *          Event object to be sent
   */
  public static void sendEventUpdate(Integer calendarId, EventResponse event) {
    sendObj(calendarId, MessageType.EVENT_UPDATE, event);
  }

  /**
   * Sends a "delete/event" to subscribers to the calendar
   * 
   * @param calendarId
   *          CalendarId of which the ONLINE subscribers will be notified
   * @param event
   *          Event object to be sent
   */
  public static void sendEventDelete(Integer calendarId, EventResponse event) {
    sendObj(calendarId, MessageType.EVENT_DELETE, event);
  }

  /**
   * Sends a "delete/calendar" to subscribers to the calendar
   * 
   * @param calendarId
   *          CalendarId of which the ONLINE subscribers will be notified
   * @param calendar
   *          CalendarResponse to be sent
   */
  public static void sendCalendarDelete(Integer calendarId,
      CalendarResponse calendar) {
    sendObj(calendarId, MessageType.CALENDAR_DELETE, calendar);
  }

  /**
   * Sends a "update/calendar" to subscribers to the calendar
   * 
   * @param calendarId
   *          CalendarId of which the ONLINE subscribers will be notified
   * @param calendar
   *          CalendarResponse to be sent
   */
  public static void sendCalendarUpdate(Integer calendarId,
      CalendarResponse calendar) {
    sendObj(calendarId, MessageType.CALENDAR_UPDATE, calendar);
  }

}
