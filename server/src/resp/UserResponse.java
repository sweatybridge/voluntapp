package resp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import exception.InconsistentDataException;
import exception.UserNotFoundException;

import sql.SQLInsert;
import sql.SQLQuery;
import sql.SQLUpdate;

/**
 * A successful response to a user request.
 */
public class UserResponse extends Response implements SQLQuery, SQLUpdate, SQLInsert {

  private static final String EMAIL_COLUMN = "EMAIL";
  private static final String PASSWORD_COLUMN = "PASSWORD";
  private static final String FIRST_NAME_COLUMN = "FIRST_NAME";
  private static final String LAST_NAME_COLUMN = "LAST_NAME";
  private static final String ID_COLUMN = "ID";
  private static final String LAST_SEEN_COLUMN = "LAST_SEEN";
  public static final int INVALID_USER_ID = -1;

  /**
   * User details returned to the client.
   */
  private String email;
  private String firstName;
  private String lastName;
  private int userId;
  private Timestamp lastSeen;
  /**
   * Fields excluded from serialisation.
   */
  private transient ResultSet rs;
  private transient boolean found;
  private transient String hashedPassword;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public UserResponse() {
  }

  /**
   * Construct a successful user response with the given email, hashed password
   * and userId.
   * 
   * @param email
   *          Email of the user response
   * @param hashedPassword
   *          Password found in the database
   * @param userId
   *          The ID of the user requests
   */
  public UserResponse(String email, String hashedPassword, int userId,
      String firstName, String lastName) {
    this.email = email;
    this.hashedPassword = hashedPassword;
    this.userId = userId;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  private void setUserResponse() throws SQLException {
    this.email = rs.getString(EMAIL_COLUMN);
    this.hashedPassword = rs.getString(PASSWORD_COLUMN);
    this.userId = rs.getInt(ID_COLUMN);
    this.firstName = rs.getString(FIRST_NAME_COLUMN);
    this.lastName = rs.getString(LAST_NAME_COLUMN);
    this.lastSeen = rs.getTimestamp(LAST_SEEN_COLUMN);
  }

  @Override
  public String getSQLQuery() {
    return String.format("SELECT * FROM public.\"USER\" WHERE \"%s\"='%s';",
        (email == null) ? ID_COLUMN : EMAIL_COLUMN, (email == null) ? userId
            : email);
  }
  
  @Override
  public String getSQLInsert() {
    return "INSERT INTO public.\"USER\" VALUES(DEFAULT, '" + email + "','" 
        + hashedPassword + "','" + firstName + "','" + lastName + "', DEFAULT);";
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
        + ((hashedPassword == null || found++ == Integer.MIN_VALUE) ? ""
            : "\"PASSWORD\"='" + hashedPassword + "',");
    return (found == 0) ? null : String.format(
        "UPDATE public.\"USER\" SET %s WHERE \"ID\"=%d",
        formatString.substring(0, formatString.length() - 1), userId);
  }

  public String getEmail() {
    return email;
  }

  public String getHashedPassword() {
    return hashedPassword;
  }

  public int getUserId() {
    return userId;
  }

  @Override
  public void checkResult(int rowsAffected) {
    return;
  }

  @Override
  public void setResult(ResultSet result) {
    this.rs = result;
    try {
      found = rs.next();
      setUserResponse();
    } catch (SQLException e) {
      System.err.println("Error getting the result");
      return;
    }
  }
  
  public void checkResult() throws UserNotFoundException, InconsistentDataException, SQLException {
    if(found == false) {
      throw new UserNotFoundException("The users information could not be found");
    }
    if(rs.next() == true) {
      throw new InconsistentDataException("The database is dead!");
    }
  }

}
