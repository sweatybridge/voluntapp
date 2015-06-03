package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import sql.SQLInsert;
import sql.SQLQuery;
import sql.SQLUpdate;
import exception.InconsistentDataException;
import exception.UserNotFoundException;

/**
 * A successful response to a user request.
 */
public class UserResponse extends Response implements SQLQuery, SQLUpdate,
    SQLInsert {

  public static final String EMAIL_COLUMN = "EMAIL";
  private static final String PASSWORD_COLUMN = "PASSWORD";
  private static final String FIRST_NAME_COLUMN = "FIRST_NAME";
  private static final String LAST_NAME_COLUMN = "LAST_NAME";
  public static final String ID_COLUMN = "ID";
  private static final String LAST_SEEN_COLUMN = "LAST_SEEN";
  public static final String VALIDATION_KEY_COLUMN = "VALIDATION_KEY";
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
  private transient String validationCode = "$";

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

  public UserResponse(int userId, String validationCode) {
    this.userId = userId;
    this.validationCode = validationCode;
  }

  private void setUserResponse() throws SQLException {
    this.email = rs.getString(EMAIL_COLUMN);
    this.hashedPassword = rs.getString(PASSWORD_COLUMN);
    this.userId = rs.getInt(ID_COLUMN);
    this.firstName = rs.getString(FIRST_NAME_COLUMN);
    this.lastName = rs.getString(LAST_NAME_COLUMN);
    this.lastSeen = rs.getTimestamp(LAST_SEEN_COLUMN);
    this.validationCode = rs.getString(VALIDATION_KEY_COLUMN);
  }

  @Override
  public String getSQLQuery() {
    return String.format("SELECT * FROM public.\"USER\" WHERE \"%s\"=?;",
        (email == null) ? ID_COLUMN : EMAIL_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    if (email == null) {
      prepared.setInt(1, userId);
    } else {
      prepared.setString(1, escape(email));
    }
  }

  @Override
  public String getSQLInsert() {
    return String
        .format(
            "INSERT INTO public.\"USER\" (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\") VALUES(?, ?, ?, ?, ?);",
            EMAIL_COLUMN, PASSWORD_COLUMN, FIRST_NAME_COLUMN, LAST_NAME_COLUMN,
            VALIDATION_KEY_COLUMN);
  }

  @Override
  public void formatSQLInsert(PreparedStatement prepared) throws SQLException {
    int i = 1;
    prepared.setString(i++, escape(email));
    prepared.setString(i++, escape(hashedPassword));
    prepared.setString(i++, escape(firstName));
    prepared.setString(i++, escape(lastName));
    prepared.setString(i++, escape(validationCode));
  }

  @Override
  public String getSQLUpdate() {
    int found = 0;
    String formatString = ((email == null || found++ == Integer.MIN_VALUE) ? ""
        : String.format("\"%s\"=?,", EMAIL_COLUMN))
        + ((firstName == null || found++ == Integer.MIN_VALUE) ? "" : String
            .format("\"%s\"=?,", FIRST_NAME_COLUMN))
        + ((lastName == null || found++ == Integer.MIN_VALUE) ? "" : String
            .format("\"%s\"=?,", LAST_NAME_COLUMN))
        + ((hashedPassword == null || found++ == Integer.MIN_VALUE) ? ""
            : String.format("\"%s\"=?,", PASSWORD_COLUMN))
        + ((validationCode == null || found++ == Integer.MIN_VALUE) ? ""
            : String.format("\"%s\"=?,", VALIDATION_KEY_COLUMN));
    return (found == 0) ? null : String.format(
        "UPDATE public.\"USER\" SET %s WHERE \"ID\"=?",
        formatString.substring(0, formatString.length() - 1));
  }

  @Override
  public void formatSQLUpdate(PreparedStatement prepared) throws SQLException {
    int i = 1;
    if (email != null)
      prepared.setString(i++, escape(email));
    if (firstName != null)
      prepared.setString(i++, escape(firstName));
    if (lastName != null)
      prepared.setString(i++, escape(lastName));
    if (hashedPassword != null)
      prepared.setString(i++, escape(hashedPassword));
    if (validationCode != null)
      prepared.setString(i++, escape(validationCode));
    prepared.setInt(i++, userId);
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

  public void checkResult() throws UserNotFoundException,
      InconsistentDataException, SQLException {
    if (found == false) {
      throw new UserNotFoundException(
          "The users information could not be found");
    }
    if (rs.next() == true) {
      throw new InconsistentDataException("The database is dead!");
    }
  }

  public void setValidationCode(String validationCode) {
    this.validationCode = validationCode;
  }

  public String getValidationCode() {
    return validationCode;
  }

}
