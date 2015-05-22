package db;

import java.sql.Timestamp;

public class SessionInsert implements SQLInsert {
  
  private String sid;
  private Integer user;
  private static final long SESSION_LENGTH = 3600000; 
  
  public SessionInsert(String sid, Integer user) {
    this.sid = sid;
    this.user = user;
  }
  
  private Timestamp generateTimestamp() {
    long time = System.currentTimeMillis();
    return new Timestamp(time + SESSION_LENGTH);
  }

  @Override
  public String getSQLInsert() {
    Timestamp valid_until = generateTimestamp();
    // INSERT INTO "SESSIONS" VALUES ('1', 1, '1999-01-08 04:05:06');
    return "INSERT INTO \"SESSIONS\" VALUES ('" + sid + "', " + user + ", " 
        + "'" + valid_until + "');";
  }
}
