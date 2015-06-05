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
  private static final Gson GSON = new Gson();
  private static final DBInterface db = new DBInterface(DataSourceProvider.getSource());

  private static final String GUEST_PREFIX = "Guest";
  private static final AtomicInteger connectionIds = new AtomicInteger(0);
  private static final ConcurrentMap<Integer, List<Session>> connections = new ConcurrentHashMap<Integer, List<Session>>();

  @OnOpen
  public void onOpen(Session session) {
    // Authenticate user
    Map<String, List<String>> params = session.getRequestParameterMap();
    List<String> tokens = params.get("token");
    if (tokens == null || tokens.size() < 1) {
      try {
        session.getBasicRemote().sendText("No token."); // TODO: Remove after debugging
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
        session.getBasicRemote().sendText("Invalid token."); // TODO: Remove after debugging
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
    session.getUserProperties().put("userid", userId);
    
    // Add it to the active list of connections
    List<Session> sessions = connections.get(userId);
    if (sessions == null) {
      List<Session> userSessions = new ArrayList<>();
      userSessions.add(session);
      connections.put(Integer.valueOf(userId), userSessions);
      return;
    }
    sessions.add(session);
    try {
      session.getBasicRemote().sendText("Welcome");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @OnClose
  public void onClose(Session session) {
    // TODO: Remove this session from the connections map
  }

  @OnMessage
  public void onMessage(String message) {
    ChatMessage chatMessage = GSON.fromJson(message, ChatMessage.class);
    sendChatMessage(chatMessage, true);
  }

  @OnError
  public void onError(Throwable t) throws Throwable {
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
          session.getBasicRemote().sendText(GSON.toJson(chatMessage));
        } catch (IOException e) {
          e.printStackTrace();
          System.err.println("Couldn't send ChatMessage.");
        }
      }

    }
  }

}
