package sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SQLInsert {
  public String getSQLInsert();

  public void formatSQLInsert(PreparedStatement prepared) throws SQLException;
}
