package db;


public class SessionDelete implements SQLUpdate {
  
  private final String sid;
  
  public SessionDelete(String sid) {
    this.sid = sid;
  }
  
  @Override
  public String getSQLUpdate() {
    StringBuilder builder = new StringBuilder();
    builder.append("DELETE FROM \"SESSIONS\" WHERE \"SID\"='")
      .append(sid).append("';");
    return builder.toString();
  }

  @Override
  public void checkResult(int rowsAffected) {
    assert rowsAffected == 1;
  }
  
}
