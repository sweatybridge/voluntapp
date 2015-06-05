package resp;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MessageResponse extends Response {

  private String type;
  private int from;
  private int to;
  private String payload;
  private Timestamp timestamp;

  public MessageResponse() {
  };

  public MessageResponse(String type, int from, int to, String payload) {
    this.type = type;
    this.from = from;
    this.to = to;
    this.payload = payload;
  }

  @Override
  public String getSQLInsert() {
    // Params, 1st - from id, 2nd - to id, 3rd - payload, 4th - type.
    return String.format(
        "INSERT INTO \"MESSAGE\" VALUES (%s, ?, ?, %s, ?, ?);", "DEFAULT",
        "DEFAULT");
  }

  @Override
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {
    int i = 1;
    prepared.setInt(i++, from);
    prepared.setInt(i++, to);
    prepared.setString(i++, escape(payload));
    prepared.setString(i++, escape(type));
  }

}
