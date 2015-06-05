package chat;

import java.util.ArrayList;
import java.util.List;

import resp.RosterResponse;


public class ChatMessage {
  private String type;
  private List<Integer> destinationIds;
  private Integer sourceId;
  private String payload;
  
  // Empty constructor
  public ChatMessage() {
    
  }

  public ChatMessage(String type, List<Integer> destinationIds,
      Integer sourceId, String payload) {
    this.type = type;
    this.destinationIds = destinationIds;
    this.sourceId = sourceId;
    this.payload = payload;
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

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }
}
