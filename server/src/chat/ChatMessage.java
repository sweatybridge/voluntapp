package chat;

import java.util.Date;
import java.util.List;

import com.google.gson.Gson;

public class ChatMessage {
  private static final Gson gson = new Gson();

  private String type;
  private List<Integer> destinationIds;
  private Integer sourceId;
  private Date date;
  private Object payload;

  // Empty constructor
  public ChatMessage() {

  }

  public ChatMessage(String type, List<Integer> destinationIds,
      Integer sourceId, Object payload) {
    this.type = type;
    this.destinationIds = destinationIds;
    this.sourceId = sourceId;
    this.setDate(new Date());
    this.payload = payload;
  }

  /**
   * Creates a ChatMessage instance from the given json string.
   * 
   * @param json
   *          JSON string that represents a ChatMessage
   * @param setToNow
   *          Sets ChatMessage date stamp to now
   * @return Intance of ChatMessage represented by json provided
   */
  public static ChatMessage fromJson(String json, boolean setToNow) {
    ChatMessage cMessage = gson.fromJson(json, ChatMessage.class);
    // If requested, set date to now
    if (setToNow) {
      cMessage.setDate(new Date());
    }
    return cMessage;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<Integer> getDestinationIds() {
    return destinationIds;
  }

  public void setDestinationIds(List<Integer> destinationIds) {
    this.destinationIds = destinationIds;
  }

  public Integer getSourceId() {
    return sourceId;
  }

  public void setSourceId(Integer sourceId) {
    this.sourceId = sourceId;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getPayloadString() {
    return gson.toJson(payload);
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  @Override
  public String toString() {
    return gson.toJson(this);
  }
}
