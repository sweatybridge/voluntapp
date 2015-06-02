package resp;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ValidationResponse extends Response {

  private String validationCode;
  private Integer userId;
  private Boolean valid;

  public ValidationResponse(Integer userId, String validationCode) {
    this.userId = userId;
    this.validationCode = validationCode;
    this.valid = false;
  }

  @Override
  public String getSQLUpdate() {
    return String
        .format(
            "UPDATE \"USER\" SET \"%s\"=NULLIF(\"%s\", ?) WHERE \"%s\"=? AND \"%s\"=?;",
            UserResponse.VALIDATION_KEY_COLUMN,
            UserResponse.VALIDATION_KEY_COLUMN,
            UserResponse.VALIDATION_KEY_COLUMN, UserResponse.ID_COLUMN);
  }

  @Override
  public void formatSQLUpdate(PreparedStatement prepared) throws SQLException {
    int i = 1;
    prepared.setString(i++, escape(validationCode));
    prepared.setString(i++, escape(validationCode));
    prepared.setInt(i++, userId);
  }

  @Override
  public void checkResult(int rows) {
    valid = rows == 1;
  }

  public Boolean isValid() {
    return valid;
  }

}
