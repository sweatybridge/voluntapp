package chat;

import java.util.Arrays;
import java.util.Date;

import db.CalendarIdUserIdMap;
import resp.CalendarResponse;
import resp.EventResponse;

public class DynamicUpdate {
  
  private static void sendObj(Integer calendarId, MessageType mType, Object obj) {
    CalendarIdUserIdMap map = CalendarIdUserIdMap.getInstance();
    ChatMessage cm = new ChatMessage(mType.getType(),
        Arrays.asList(map.getUserIds(calendarId)), -1, new Date(), false, obj);
    ChatServer.routeChatMessage(cm);
  }
  
  public static void sendEventUpdate(Integer calendarId, EventResponse event) {
    sendObj(calendarId, MessageType.EVENT_UPDATE, event);
  }
  
  public static void sendEventDelete(Integer calendarId, EventResponse event) {
    sendObj(calendarId, MessageType.EVENT_DELETE, event);
  }
  
  public static void sendCalendarDelete(Integer calendarId, CalendarResponse calendar) {
    sendObj(calendarId, MessageType.CALENDAR_DELETE, calendar);
  }
  
  public static void sendCalendarUpdate(Integer calendarId, CalendarResponse resp) {
    sendObj(calendarId, MessageType.CALENDAR_UPDATE, resp);
  }
  
}
