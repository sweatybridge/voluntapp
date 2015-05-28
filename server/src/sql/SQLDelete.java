package sql;

public interface SQLDelete {
  public String getSQLDelete();
  public void checkResult(int rowsAffected);
}
