package chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import req.EventSubscriptionRequest;
import resp.CalendarResponse;
import resp.EventResponse;
import utils.AuthLevel;
import utils.Pair;

import com.google.common.collect.ImmutableMap;

import db.CalendarIdUserIdMap;

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
    // Check if there any online users for the calendar
    List<Pair<Integer, AuthLevel>> calendarIds = map.getUsers(calendarId);
    if (calendarIds != null) {
      // Get a list of user IDs
      List<Integer> userIds = new ArrayList<Integer>();
      for (Pair<Integer, AuthLevel> pair : calendarIds) {
        userIds.add(pair.getKey());
      }
      //System.out.println("User ID to which object is sent: " + userIds.toString());
      ChatMessage cm = new ChatMessage(mType.getType(),
          userIds, -1, false, obj);
      ChatServer.routeChatMessage(cm);
    }
  }

  /**
   * Notifies every online user in the given calendar Ids that the given user Id
   * came online
   * 
   * @param calendarIds
   *          Set of calendar Ids of which the people will be notified
   * @param userId
   *          The user Id which came online
   */
  public static void sendOnlineUser(Set<Integer> calendarIds, Integer userId) {
    for (Integer cid : calendarIds) {
      sendObj(cid, MessageType.USER_ONLINE, ImmutableMap.of("userId", userId));
    }
  }

  /**
   * Notifies every online user in the given calendar Ids that the given user Id
   * went offline
   * 
   * @param calendarIds
   *          Set of calendar Ids of which the people will be notified
   * @param userId
   *          The user Id which went offline
   */
  public static void sendOfflineUser(Set<Integer> calendarIds, Integer userId) {
    for (Integer cid : calendarIds) {
      sendObj(cid, MessageType.USER_OFFLINE, ImmutableMap.of("userId", userId));
    }
  }

  /**
   * Sends an "update/event" to subscribers to the calendar
   * 
   * @param calendarId
   *          CalendarId of which the ONLINE subscribers will be notified
   * @param event
   *          Event object to be sent
   * @param all
   *          If set, the message is sent to all subscribers, otherwise
   *          only to editors and admins.
   */
  public static void sendEventUpdate(Integer calendarId, EventResponse event, 
      boolean all) {
    if (all) {
      //System.out.println("Sending event to everyone.");
      sendObj(calendarId, MessageType.EVENT_UPDATE, event);
    } else {
      // Send the message to editors and admins only
      CalendarIdUserIdMap map = CalendarIdUserIdMap.getInstance();
      // Check if there any online users for the calendar
      List<Pair<Integer, AuthLevel>> calendarIds = map.getUsers(calendarId);
      if (calendarIds != null) {
        // Get a list of IDs of admins and editors
        List<Integer> userIds = new ArrayList<Integer>();
        for (Pair<Integer, AuthLevel> pair : calendarIds) {
          if (pair.getValue() == AuthLevel.ADMIN || pair.getValue() == AuthLevel.EDITOR) {
            userIds.add(pair.getKey());
          }
        }
        ChatMessage cm = new ChatMessage(MessageType.EVENT_UPDATE.getType(),
            userIds, -1, false, event);
        ChatServer.routeChatMessage(cm);
      }
      /* Update only calendar editors and admins. */
    }
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
    sendObj(calendarId, MessageType.EVENT_DELETE, ImmutableMap.of("eventId", event.getEventId(), "calendarId", event.getCalendarId()));
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
   * Sends a joined update to the current subscribers.
   * 
   * @param calendarId
   *          People online on this calendar will get notified
   * @param resp
   *          map object to be sent
   */
  public static void sendCalendarJoin(Integer calendarId, ImmutableMap<String, Object> immutableMap) {
    sendObj(calendarId, MessageType.CALENDAR_JOIN, immutableMap);
  }
  
  /**
   * Sends a unjoined update to the current subscribers.
   * 
   * @param calendarId
   *          People online on this calendar will get notified
   * @param resp
   *          map object to be sent
   */
  public static void sendCalendarUnjoin(Integer calendarId, ImmutableMap<String, Object> immutableMap) {
    sendObj(calendarId, MessageType.CALENDAR_UNJOIN, immutableMap);
  }

  /**
   * Sends an event join update when somebody joins an event
   * 
   * @param calenderId
   *          CalendarId of the event, online people in the calendar will be
   *          notified
   * @param req
   *          EventSubscriptionRequest that is sent
   */
  public static void sendEventJoin(Integer calenderId,
      EventSubscriptionRequest req) {
    sendObj(calenderId, MessageType.EVENT_JOIN, req);
  }

  /**
   * Sends an event unjoin update when somebody joins an event
   * 
   * @param calenderId
   *          CalendarId of the event, online people in the calendar will be
   *          notified
   * @param req
   *          EventSubscriptionRequest that is sent
   */
  public static void sendEventUnJoin(Integer calenderId,
      EventSubscriptionRequest req) {
    sendObj(calenderId, MessageType.EVENT_UNJOIN, req);
  }

}
