package sql;

public interface SQLUpdate {
  public String getSQLUpdate();
  public void checkResult(int rowsAffected);
}
