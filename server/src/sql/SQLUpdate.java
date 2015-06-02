package sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SQLUpdate {
  public String getSQLUpdate();

  public void formatSQLUpdate(PreparedStatement prepare) throws SQLException;

  public void checkResult(int rowsAffected);
}
