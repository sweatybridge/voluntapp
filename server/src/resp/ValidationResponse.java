package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import exception.InconsistentDataException;

public class ValidationResponse extends Response {

  private String validationCode;
  private Integer userId;
  private Boolean valid;
  private ResultSet rs;

  public ValidationResponse(Integer userId, String validationCode) {
    this.userId = userId;
    this.validationCode = validationCode;
    this.valid = false;
  }

  @Override
  public String getSQLQuery() {
    return String.format(
        "SELECT 1 FROM \"USER\" WHERE \"%s\" = ? AND \"%s\" = ?;",
        UserResponse.ID_COLUMN, UserResponse.VALIDATION_KEY_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    int i = 1;
    prepared.setInt(i++, userId);
    prepared.setString(i++, escape(validationCode));
  }

  @Override
  public void setResult(ResultSet rs) {
    this.rs = rs;
  }

  public Boolean isValid() throws InconsistentDataException, SQLException {
    valid = rs.next();
    if (rs.next()) {
      throw new InconsistentDataException("Users table is inconsistant!");
    }
    return valid;
  }

}
