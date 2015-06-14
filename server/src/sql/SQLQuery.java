package sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface to hold the methods required by the {@link db.DBInterface
 * DBInterface} for performing a query operation.
 * 
 * @author bs2113
 * 
 */
public interface SQLQuery {
  /**
   * Creates the query string with empty fields.
   * 
   * @return The SQl statement used to perform the query operation. Used within
   *         {@link db.DBInterface#query(SQLQuery) query}.}
   */
  public String getSQLQuery();

  /**
   * Formats the {@link java.sql.PreparedStatement PreparedStatement} made from
   * {@link sql.SQLQuery#getSQLQuery() getSQLQuery} by filling in the empty
   * query fields.
   * 
   * @param prepared
   *          Prepared statement to be formated
   * @throws SQLException
   * 
   */
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException;

  /**
   * Sets the relevant fields in the query with data returned from the database.
   * 
   * @param result
   *          Data returned from the database.
   */
  public void setResult(ResultSet result);
}
