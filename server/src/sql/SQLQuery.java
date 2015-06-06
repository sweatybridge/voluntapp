package sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface to hold enforce the method required by the {@link db.DBInterface
 * DBInterdave} for performing a query operation.
 * 
 * @author bs2113
 * 
 */
public interface SQLQuery {
  /**
   * @return The SQl statement used to perform the query operation. Used within
   *         {@link db.DBInterface#query(SQLQuery) query}.}
   */
  public String getSQLQuery();

  /**
   * Formats the {@link java.sql.PreparedStatement PreparedStatement} made from 
   * 
   * @param prepared
   * @throws SQLException
   */
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException;

  public void setResult(ResultSet result);
}
