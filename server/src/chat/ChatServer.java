package chat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import resp.RosterResponse;
import resp.RosterResponse.RosterEntry;
import resp.SessionResponse;
import utils.ConcurrentHashSet;
import utils.DataSourceProvider;
import db.CalendarIdUserIdMap;
import db.DBInterface;
import exception.SessionNotFoundException;

/**
 * Listens to websocket connections for a general data relay platform. It
 * authenticates the connecting users based on the session token they provide as
 * a request parameter. The connections are stored based on the userId. When
 * ever a message is received it is routed according to the destinationIds of
 * the ChatMessage.
 * 
 * @author nc1813
 * 
 */
@ServerEndpoint(value = "/chat")
public class ChatServer {
  private static final DBInterface db = new DBInterface(
      DataSourceProvider.getSource());

  private static final ConcurrentMap<Integer, ConcurrentHashSet<Session>> connections = new ConcurrentHashMap<Integer, ConcurrentHashSet<Session>>();

  /**
   * Callback function invoked on websocket session start. Checks token and
   * authenticates user. Expects token as a get parameter in the url, ?token=..
   * Then the user roster is sent, and then the offline messages of the user.
   * The session is stored in the connections map afterwards.
   * 
   * @param session
   *          Session that represents the opened connection
   */
  @OnOpen
  public void onOpen(Session session) {
    // Authenticate user
    Map<String, List<String>> params = session.getRequestParameterMap();
    List<String> tokens = params.get("token");
    if (tokens == null || tokens.size() < 1) {
      try {
        session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
            "No authentication token provided."));
      } catch (IOException e) {
        e.printStackTrace();
        System.err.println("Could not close unauthorized session.");
      }
      return;
    }

    // Check token provided
    SessionResponse sessionResponse = null;
    try {
      sessionResponse = db.getSession(tokens.get(0));
    } catch (SQLException | SessionNotFoundException e) {
      try {
        session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
            "Invalid authentication token provided."));
      } catch (IOException e1) {
        e.printStackTrace();
        System.err.println("Could not close invalid authentication session.");
      }
      return;
    }

    assert (sessionResponse != null);
    // Add user to the connections map
    int userId = sessionResponse.getUserId();
    // Attach userid to the session
    session.getUserProperties().put("userId", userId);

    // Add it to the active list of connections
    boolean userCameOnline = false;
    ConcurrentHashSet<Session> sessions = connections.get(userId);
    if (sessions == null) {
      ConcurrentHashSet<Session> userSessions = new ConcurrentHashSet<>();
      userSessions.add(session);
      connections.put(Integer.valueOf(userId), userSessions);
      userCameOnline = true;
    } else {
      sessions.add(session);
    }

    try {
      RosterResponse roster = db.getRoster(userId);
      // Signal that the user came online if we have to
      if (userCameOnline) {
        Set<Integer> calendarIds = new HashSet<Integer>();
        for (RosterEntry entry : roster.getRosterEntries()) {
          calendarIds.addAll(entry.getcids());
          entry.setIsOnline(connections.get(entry.getuid()) != null);
        }
        /* Record that a user is subscribed to given calendars. */
        CalendarIdUserIdMap map = CalendarIdUserIdMap.getInstance();
        for (Integer calendarId : calendarIds) {
          map.put(calendarId, userId, db.authoriseUser(calendarId, userId));
        }
        DynamicUpdate.sendOnlineUser(calendarIds, userId);
      }

      // Send the actual user roster
      List<Integer> destinationIds = new ArrayList<Integer>(2);
      destinationIds.add(userId);
      ChatMessage rosterMessage = new ChatMessage("roster", destinationIds, -1,
          false, roster);
      routeChatMessage(rosterMessage);

      // Return any offline messages
      List<ChatMessage> cms = db.getMessages(userId);
      if (cms != null) {
        for (ChatMessage cm : cms) {
          routeChatMessage(cm);
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
      System.err.println("Failed to get offline message for user: " + userId);
    }

  }

  /**
   * Callback function invoked on websocket session close. Updates connections
   * map and CalendarIdUserIdMap.
   * 
   * @param session
   *          Session that has been closed.
   */
  @OnClose
  public void onClose(Session session) {
    // Remove this session from the connections map
    Integer userId = (Integer) session.getUserProperties().get("userId");
    if (userId != null) {
      ConcurrentHashSet<Session> sessions = connections.get(userId);
      sessions.remove(session);
      // Check if the user is logged off entirely
      if (sessions.isEmpty()) {
        // Remove the HashSet from the mapping, it will be garbage collected
        connections.remove(userId);
        // Remove the calendar Id map
        Set<Integer> calendarIds = CalendarIdUserIdMap.getInstance()
            .deleteUser(userId);
        // Signal that the user actually went offline
        DynamicUpdate.sendOfflineUser(calendarIds, userId);
      }
    }
  }

  /**
   * Callback function for websocket onMessage. Deserializes message and calls
   * routeChatMessage
   * 
   * @param message
   *          JSON string of a ChatMessage
   */
  @OnMessage
  public void onMessage(String message) {
    // Get ChatMessage and set time to now so it can't be forged
    ChatMessage chatMessage = ChatMessage.fromJson(message, true);
    routeChatMessage(chatMessage);
  }

  /**
   * Callback function invoked on websocket errors, prints stack trace and logs
   * error.
   * 
   * @param t
   *          Error that has occurred of which the stack trace is printed.
   * @throws Throwable
   *           Ignored by the current implementation
   */
  @OnError
  public void onError(Throwable t) throws Throwable {
    System.err.println("ChatServer Error: " + t.toString());
    t.printStackTrace();
  }

  /**
   * Forwards the chatMessage to the destinationIds. Can store messages in the
   * database if the user is offline. When routing the destinationIds are NOT
   * sent.
   * 
   * @param chatMessage
   *          The ChatMessage that will be relayed.
   * @param storeOffline
   *          If true, then if the destination is offline, will store it in the
   *          database.
   */
  public static void routeChatMessage(ChatMessage chatMessage) {
    List<Integer> destinationIds = chatMessage.getDestinationIds();
    // Make sure we have destinations to route to
    if (destinationIds == null) {
      return;
    }
    // Set it to null for gson not to serialize
    chatMessage.setDestinationIds(null);
    // For every destination id
    for (Integer destinationId : destinationIds) {
      // Check if it was addressed at the server, we currently handle only pings
      if (destinationId == -1 && chatMessage.getType().equals("ping")
          && chatMessage.getSourceId() != -1) {
        // Ping back
        Integer[] destIds = new Integer[] { chatMessage.getSourceId() };
        ChatMessage pong = new ChatMessage("pong", Arrays.asList(destIds), -1,
            false, null);
        routeChatMessage(pong);
        continue;
      }

      // Get their list of active sessions
      ConcurrentHashSet<Session> sessions = connections.get(destinationId);
      if ((sessions == null || sessions.isEmpty())
          && chatMessage.isStoreOffline()) {
        // Store message offline
        try {
          db.insertMessage(chatMessage, destinationId);
        } catch (SQLException e) {
          System.err.println("Unable to store offline message to "
              + destinationId);
          e.printStackTrace();
        }
        continue;
      }

      // Send to each session
      for (Session session : sessions) {
        try {
          session.getBasicRemote().sendText(chatMessage.toString());
        } catch (IOException e) {
          e.printStackTrace();
          System.err.println("Couldn't send ChatMessage.");
        }
      }

    }
  }

}
