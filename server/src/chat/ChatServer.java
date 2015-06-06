package chat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import resp.SessionResponse;
import utils.DataSourceProvider;
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

  private static final ConcurrentMap<Integer, List<Session>> connections = new ConcurrentHashMap<Integer, List<Session>>();

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
    List<Session> sessions = connections.get(userId);
    if (sessions == null) {
      List<Session> userSessions = new LinkedList<>();
      userSessions.add(session);
      connections.put(Integer.valueOf(userId), userSessions);
    } else {
      sessions.add(session);
    }

    // Return to the user roster
    try {
      List<Integer> destinationIds = new ArrayList<Integer>(2);
      destinationIds.add(userId);
      ChatMessage roster = new ChatMessage("roster", destinationIds, -1,
          false, db.getRoster(userId));
      session.getBasicRemote().sendText(roster.toString());
    } catch (IOException | SQLException e) {
      e.printStackTrace();
      System.err.println("Could not send user roster at start.");
    }

    // TODO: Return any offline messages
  }

  @OnClose
  public void onClose(Session session) {
    // Remove this session from the connections map
    Integer userId = (Integer) session.getUserProperties().get("userId");
    if (userId != null) {
      connections.get(userId).remove(session);
    }
  }

  @OnMessage
  public void onMessage(String message) {
    // Get ChatMessage and set time to now so it can't be forged
    ChatMessage chatMessage = ChatMessage.fromJson(message, true);
    System.out.println("Routing: "+chatMessage.toString());
    sendChatMessage(chatMessage);
  }

  @OnError
  public void onError(Throwable t) throws Throwable {
    t.printStackTrace();
    System.err.println("ChatServer Error: " + t.toString());
  }

  /**
   * Forwards the chatMessage to the destinationIds. Can store messages in the
   * database if the user is offline
   * 
   * @param chatMessage
   *          The ChatMessage that will be relayed.
   * @param storeOffline
   *          If true, then if the destination is offline, will store it in the
   *          database
   */
  public static void sendChatMessage(ChatMessage chatMessage) {
    // For every destination id
    for (Integer destinationId : chatMessage.getDestinationIds()) {
      // Check if it was addressed at the server
      if (destinationId == -1) {
        // TODO: Handle server message if any
        continue;
      }

      // Get their list of active sessions
      List<Session> sessions = connections.get(destinationId);
      if ((sessions == null || sessions.size() == 0) && chatMessage.isStoreOffline()) {
        // Store message offline
        try {
          db.insertMessage(chatMessage, destinationId);
        } catch (SQLException e) {
          System.err.println("Unable to store offline message to "+destinationId);
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
