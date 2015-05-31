package sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface SQLQuery {
  public String getSQLQuery();

  public void formatSQLQuery(PreparedStatement prepared) throws SQLException;

  public void setResult(ResultSet result);
}
