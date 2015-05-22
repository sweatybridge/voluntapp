package db;

import java.sql.ResultSet;

public interface SQLQuery {
  public String getSQLQuery();
  public void setResult(ResultSet result, int rowsAffected) throws UserNotFoundException;
}
