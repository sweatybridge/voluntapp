package sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SQLDelete {
  public String getSQLDelete();

  public void formatSQLDelete(PreparedStatement prepared) throws SQLException;

}
