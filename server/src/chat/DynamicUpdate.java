package chat;

import java.util.Arrays;
import java.util.Date;

import db.CalendarIdUserIdMap;
import resp.EventResponse;

public class DynamicUpdate {
  
  public static void sendEventUpdate(Integer calendarId, EventResponse event) {
    CalendarIdUserIdMap map = CalendarIdUserIdMap.getInstance();
    ChatMessage message = new ChatMessage(MessageType.EVENT_UPDATE.getType(),
        Arrays.asList(map.getUserIds(calendarId)), -1, new Date(), false, event);
    ChatServer.routeChatMessage(message);
  }  
}
