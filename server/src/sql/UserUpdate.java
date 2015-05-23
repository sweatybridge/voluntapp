package sql;

public class UserUpdate implements SQLUpdate {

  public int userId;
  public String email;
  public String firstName;
  public String lastName;
  public String password;

  public UserUpdate(int userId, String email, String firstName,
      String lastName, String password) {
    this.userId = userId;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.password = password;
  }

  @Override
  public String getSQLUpdate() {
    int found = 0;
    String formatString = ((email == null || found++ == Integer.MIN_VALUE) ? ""
        : "\"EMAIL\"='" + email + "',")
        + ((firstName == null || found++ == Integer.MIN_VALUE) ? ""
            : "\"FIRST_NAME\"='" + firstName + "',")
        + ((lastName == null || found++ == Integer.MIN_VALUE) ? ""
            : "\"LAST_NAME\"='" + lastName + "',")
        + ((password == null || found++ == Integer.MIN_VALUE) ? ""
            : "\"PASSWORD\"='" + password + "',");
    return (found == 0) ? null : String.format(
        "UPDATE public.\"USERS\" SET %s WHERE \"ID\"=%d",
        formatString.substring(0, formatString.length() - 1), userId);
  }

  @Override
  public void checkResult(int rowsAffected) {
    return;
  }
}
