package chat;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import resp.SessionResponse;

import utils.DataSourceProvider;

import com.google.gson.Gson;

import db.DBInterface;
import exception.SessionNotFoundException;

@ServerEndpoint(value = "/chat")
public class ChatServer {
  private static final Gson gson = new Gson();
  private static final DBInterface db = new DBInterface(DataSourceProvider.getSource());

  private static final ConcurrentMap<Integer, List<Session>> connections = new ConcurrentHashMap<Integer, List<Session>>();

  @OnOpen
  public void onOpen(Session session) {
    // Authenticate user
    Map<String, List<String>> params = session.getRequestParameterMap();
    List<String> tokens = params.get("token");
    if (tokens == null || tokens.size() < 1) {
      try {
        session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "No authentication token provided."));
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
        session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid authentication token provided."));
      } catch (IOException e1) {
        e.printStackTrace();
        System.err.println("Could not close invalid authentication session.");
      }
      return;
    }
    
    assert(sessionResponse != null);
    // Add user to the connections map
    int userId = sessionResponse.getUserId();
    // Attach userid to the session
    session.getUserProperties().put("userId", userId);
    
    // Add it to the active list of connections
    List<Session> sessions = connections.get(userId);
    if (sessions == null) {
      List<Session> userSessions = new ArrayList<>();
      userSessions.add(session);
      connections.put(Integer.valueOf(userId), userSessions);
    } else {
      sessions.add(session);
    }
    
    // Return to the user roster
    try {
      session.getBasicRemote().sendText("Roster incoming:");
      //ChatMessage roster = new ChatMessage(db.getRoster(userId));
      session.getBasicRemote().sendText(gson.toJson(db.getRoster(userId)));
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
    connections.get(userId).remove(session);
  }

  @OnMessage
  public void onMessage(String message) {
    ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);
    sendChatMessage(chatMessage, true);
  }

  @OnError
  public void onError(Throwable t) throws Throwable {
    t.printStackTrace();
    System.err.println("ChatServer Error: " + t.toString());
  }

  public static void sendChatMessage(ChatMessage chatMessage,
      boolean storeOffline) {
    // For every destination id
    for (Integer destinationId : chatMessage.getDestinationIds()) {
      // Check if it was addressed at the server
      if (destinationId == -1) {
        // TODO: Handle server message if any
        return;
      }
      
      // Get their list of active sessions
      List<Session> sessions = connections.get(destinationId);
      if (sessions == null && storeOffline) {
        // TODO: Store message offline
        return;
      }

      // Send to each session
      for (Session session : sessions) {
        try {
          session.getBasicRemote().sendText(gson.toJson(chatMessage));
        } catch (IOException e) {
          e.printStackTrace();
          System.err.println("Couldn't send ChatMessage.");
        }
      }

    }
  }

}
