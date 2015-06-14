package sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface to hold the methods required by the {@link db.DBInterface
 * DBInterface} for performing a insert operation.
 * 
 * @author bs2113
 * 
 */
public interface SQLInsert {
  /**
   * Creates the insertion SQL statement with empty fields.
   * 
   * @return The SQL string to perform a insert operation. Used within
   *         {@link db.DBInterface#insert(SQLInsert) insert}.
   */
  public String getSQLInsert();

  /**
   * Formats the {@link java.sql.PreparedStatement PreparedStatement} made from
   * {@link #getSQLInsert() getSQLInsert} with the required parameters.
   * 
   * @param prepared
   *          The {@link java.sql.PreparedStatement PreparedStatement} to
   *          format.
   * @throws SQLException
   *           Thrown when there is an error in the statement formating.
   */
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException;
}
