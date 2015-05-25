package resp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import sql.SQLInsert;
import sql.SQLQuery;
import sql.SQLUpdate;

/**
 * A successful response to a login request.
 */
public class SessionResponse extends Response implements SQLInsert, SQLQuery,
    SQLUpdate {

  /**
   * Session details returned to the client.
   */
  private String sessionId;

  /**
   * Fields excluded from serialisation.
   */
  private transient Timestamp timeStamp;
  private transient int expiresIn;
  private transient int userId;
  private transient ResultSet rs;
  private transient boolean found;

  public static String USER_COLUMN = "USER";
  public static String SID_COLUMN = "SID";

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public SessionResponse() {
  }

  /**
   * Construct a successful login response with auth token.
   * 
   * @param token
   *          authorization token
   */
  public SessionResponse(String sessionId) {
    this.sessionId = sessionId;
  }

  public SessionResponse(String sessionId, int userId) {
    this.sessionId = sessionId;
    this.userId = userId;
  }

  public int getUserId() {
    return userId;
  }

  public String getSessionId() {
    return sessionId;
  }

  @Override
  public String getSQLUpdate() {
    StringBuilder builder = new StringBuilder();
    builder.append("DELETE FROM \"SESSION\" WHERE \"SID\"='").append(sessionId)
        .append("';");
    return builder.toString();
  }

  public boolean isFound() {
    return found;
  }

  @Override
  public void checkResult(int rowsAffected) {
    // TODO:
  }

  @Override
  public String getSQLQuery() {
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT * FROM \"SESSION\" WHERE " + "\"SID\"='")
        .append(sessionId).append("';");
    return builder.toString();
  }

  private void setSessionResponse() throws SQLException {
    this.sessionId = rs.getString(SID_COLUMN);
    this.userId = rs.getInt(USER_COLUMN);
  }

  @Override
  public void setResult(ResultSet result) {
    this.rs = result;
    try {
      found = result.next();
      if (found) {
        setSessionResponse();
      }
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
  }

  @Override
  public String getSQLInsert() {
    return "INSERT INTO \"SESSION\" VALUES ('" + sessionId + "', " + userId
        + ", DEFAULT);";
  }

  public String getSQLRefresh() {
    return String.format(
        "UPDATE \"SESSION\" SET \"SID\"='%s', \"START_TIME\"=now() WHERE \"USER\"=%d;", sessionId,
        userId);
  }
}
