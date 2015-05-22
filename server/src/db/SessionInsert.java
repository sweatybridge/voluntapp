package db;


public class SessionInsert implements SQLInsert {
  
  private String sid;
  private Integer user;
  
  public SessionInsert(String sid, Integer user) {
    this.sid = sid;
    this.user = user;
  }

  @Override
  public String getSQLInsert() {
    // INSERT INTO "SESSIONS" VALUES ('1', 1, '1999-01-08 04:05:06');
    return "INSERT INTO \"SESSIONS\" VALUES ('" + sid + "', " + user + 
        ", DEFAULT);";
  }
}
