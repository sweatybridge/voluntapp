package sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface to hold enforce the method required by the {@link db.DBInterface
 * DBInterdave} for performing a delete operation.
 * 
 * @author bs2113
 * 
 */
public interface SQLDelete {
  /**
   * Gets the SQL statement used for a deletion. Used within
   * {@link db.DBInterface#delete(SQLDelete) delete} to perform database
   * interaction.
   * 
   * @return The deletion query.
   */
  public String getSQLDelete();

  /**
   * Method to format the {@link java.sql.PreparedStatement PreparedStatement}
   * used by {@link db.DBInterface#delete(SQLDelete) delete} to perform database
   * interaction.
   * 
   * @param prepared
   *          The given {@link java.sql.PreparedStatement PreparedStatement} to
   *          format.
   * @throws SQLException
   *           Throw if there is a problem formating the
   *           {@link java.sql.PreparedStatement PreparedStatement}.
   */
  public void formatSQLDelete(PreparedStatement prepared) throws SQLException;

}
