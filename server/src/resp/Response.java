package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import sql.SQLDelete;
import sql.SQLInsert;
import sql.SQLQuery;
import sql.SQLUpdate;

/**
 * Represents a standard format that all API responses should conform to.
 * 
 * NOTE: GSON performs serialisation of private fields automatically.
 */
public abstract class Response implements SQLQuery, SQLInsert, SQLUpdate,
    SQLDelete {

  /**
   * Default status code of a successful response. Could also be used to trigger
   * dynamic behaviour on the client based on different status code.
   */
  protected int statusCode = 0;

  @Override
  public String getSQLDelete() {
    return null;
  }

  @Override
  public void formatSQLDelete(PreparedStatement prepared) throws SQLException {
    return;
  }

  @Override
  public String getSQLQuery() {
    return null;
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    return;
  }

  @Override
  public void setResult(ResultSet result) {
    return;
  }

  @Override
  public String getSQLInsert() {
    return null;
  }

  @Override
  public String getSQLUpdate() {
    return null;
  }

  @Override
  public void formatSQLUpdate(PreparedStatement prepare) throws SQLException {
    return;
  }

  @Override
  public void checkResult(int rowsAffected) {
    return;
  }

  @Override
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {
    return;
  }

  public String escape(String s) {
    if (s == null) {
      return null;
    }
    return s.replace("\'", "\'\'");
  }
}
