package sql;

public class SessionInsert implements SQLUpdate {
  
  private String sid;
  private Integer user;
  
  public SessionInsert(String sid, Integer user) {
    this.sid = sid;
    this.user = user;
  }

  @Override
  public String getSQLUpdate() {
    return "INSERT INTO \"SESSIONS\" VALUES ('" + sid + "', " + user + 
        ", DEFAULT);";
  }

  @Override
  public void checkResult(int rowsAffected) {
    // TODO Auto-generated method stub    
  }
}
