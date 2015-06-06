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

  /*
   * (non-Javadoc)
   * 
   * @see sql.SQLDelete#getSQLDelete()
   */
  @Override
  public String getSQLDelete() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see sql.SQLDelete#formatSQLDelete(java.sql.PreparedStatement)
   */
  @Override
  public void formatSQLDelete(PreparedStatement prepared) throws SQLException {
    return;
  }

  /*
   * (non-Javadoc)
   * 
   * @see sql.SQLQuery#getSQLQuery()
   */
  @Override
  public String getSQLQuery() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see sql.SQLQuery#formatSQLQuery(java.sql.PreparedStatement)
   */
  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    return;
  }

  /*
   * (non-Javadoc)
   * 
   * @see sql.SQLQuery#setResult(java.sql.ResultSet)
   */
  @Override
  public void setResult(ResultSet result) {
    return;
  }

  /*
   * (non-Javadoc)
   * 
   * @see sql.SQLInsert#getSQLInsert()
   */
  @Override
  public String getSQLInsert() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see sql.SQLUpdate#getSQLUpdate()
   */
  @Override
  public String getSQLUpdate() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see sql.SQLUpdate#formatSQLUpdate(java.sql.PreparedStatement)
   */
  @Override
  public void formatSQLUpdate(PreparedStatement prepare) throws SQLException {
    return;
  }

  /*
   * (non-Javadoc)
   * 
   * @see sql.SQLUpdate#checkResult(int)
   */
  @Override
  public void checkResult(int rowsAffected) {
    return;
  }

  /*
   * (non-Javadoc)
   * 
   * @see sql.SQLInsert#formatSQLInsert(java.sql.PreparedStatement)
   */
  @Override
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {
    return;
  }

  /**
   * Method used to escape all string that go into the database. This helps
   * prevent any types of SQL injections that could occur.
   * 
   * @param s
   *          The string to escape.
   * @return The given string escaped.
   */
  public String escape(String s) {
    if (s == null) {
      return null;
    }
    return s.replace("\'", "\'\'");
  }
}
