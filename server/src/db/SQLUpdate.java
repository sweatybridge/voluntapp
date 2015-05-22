package db;

import java.sql.ResultSet;

public interface SQLUpdate {
  public String getSQLUpdate();
  public void checkResult(int rowsAffected);
}
