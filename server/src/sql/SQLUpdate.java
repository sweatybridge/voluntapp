package sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface to hold the methods required by the {@link db.DBInterface
 * DBInterface} for performing an update operation.
 * 
 * @author bs2113
 * 
 */
public interface SQLUpdate {
  /**
   * Creates an SQL statement for update with empty fields.
   * 
   * @return The SQl statement used to perform the update operation. Used within
   *         {@link db.DBInterface#update update}.
   */
  public String getSQLUpdate();

  /**
   * Fills in the empty fields for the update query created with
   * {@link sql.SQLUpdate#getSQLUpdate() getSQLUpdate()} with the object fields.
   * 
   * @param prepare
   *          The statement to be formatted.
   * @throws SQLException
   *           Thrown if there is an exception in the database.
   */
  public void formatSQLUpdate(PreparedStatement prepare) throws SQLException;

  /**
   * Checks if the result had any adverse effects. For example, if more than 1
   * row is affected.
   * 
   * @param rowsAffected
   *          Number of rows this query affected in the database.
   */
  public void checkResult(int rowsAffected);
}
