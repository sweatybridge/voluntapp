package db;

import java.sql.*;

import exception.InconsistentDataException;
import exception.UserNotFoundException;

import req.RegisterRequest;
import req.SessionRequest;
import req.UserRequest;
import resp.UserResponse;
import sql.LoginQuery;
import sql.SQLQuery;
import sql.SQLUpdate;
import sql.SessionDelete;
import sql.SessionInsert;
import sql.SessionQuery;
import sql.UserUpdate;

/**
 * Class to manage the back-end to the database, deals with 
 * any and all queries.
 * 
 * @author bs2113
 * 
 */
public class DBInterface {

  // Database information and conn
  public final static String DATABASE_NAME = "jdbc:postgresql://db.doc.ic.ac.uk/";
  public final static String DATABASE_USER = "g1427134_u";
  public final static String DATABASE_PASS = "TRLzYYiVbD";
  private Connection conn;

  /**
   * Builds the interface with the given connection.
   * 
   * @param conn
   *          The connection to be used for the database.
   */
  public DBInterface(Connection conn) {
    this.conn = conn;
  }
  
  /**
   * Basic constructor, defaults to our database.
   */
  public DBInterface() {
    try {
      conn = DriverManager.getConnection(DBInterface.DATABASE_NAME, DBInterface.DATABASE_USER,
          DBInterface.DATABASE_PASS);
    } catch (SQLException e) {
      System.exit(0);
    }
  }

  /**
   * Check to make sure the user exists and used to obtain the stored password
   * 
   * @param uq
   *          User request containing the information to look up the user.
   * @return A UserResponse with the email,password and ID of the user.
   * @throws SQLException
   *           There was an error in the database.
   * @throws UserNotFoundException
   *           Thrown if the users data was not in the database.
   */
  public UserResponse verifyUser(UserRequest uq) throws SQLException,
      UserNotFoundException {

    // Get the password in and put it in the pass variable
    LoginQuery query = new LoginQuery(uq.getEmail());
    query(query);
    query.checkValid();
    return new UserResponse(uq.getEmail(), query.getPassword(), query.getID());
  }

  /**
   * Used to add a session to the database, called within the SessionManager
   * class.
   * 
   * @param sq
   *          The SessionRequest that is to be added.
   * @return Whether or not the insertion was a success.
   * @throws SQLException
   *           Thrown when there is a problem with the database interaction.
   */
  public boolean addSession(SessionRequest sq) throws SQLException {
    SessionInsert si = new SessionInsert(sq.getSessionId(), sq.getUserId());
    return insert(si);
  }

  /**
   * Updates a given users information. Uses the filled in fields in the
   * RegusterRequest to find out which ones to update, will ignore NULL fields.
   * 
   * @param userId
   *          The ID of the user to be updated.
   * @param rr
   *          The request object with the new information.
   * @return Whether or not the update with successful.
   * @throws SQLException
   *           Thrown when there is a problem with the database interaction.
   * @throws InconsistentDataException
   *           Thrown when more than one row was changed (This indicates a big
   *           problem with the information in the database).
   * @throws UserNotFoundException
   *           Thrown when the user could not be found in the database.
   */
  public boolean updateUserInfo(int userId, RegisterRequest rr)
      throws SQLException, InconsistentDataException, UserNotFoundException {
    UserUpdate uu = new UserUpdate(userId, rr.getEmail(), rr.getFirstName(),
        rr.getLastName(), rr.getPassword());
    int rows = update(uu);
    if (rows > 1) {
      throw new InconsistentDataException(
          "Update user info modified more than 1 row!");
    }
    if (rows == 0) {
      throw new UserNotFoundException(
          "The user could not be updated, as they don't exist");
    }
    return update(uu) == 1;
  }

  /**
   * Gets the user ID from a supplied session ID number. Used to find out which
   * user is logged for rights, etc.
   * 
   * @param sid
   *          The ID of the session to be looked up.
   * @return The ID of the user that it belongs to.
   * @throws SQLException
   *           Thrown when there is an error with the database interaction.
   */
  public int getUserIdFromSession(String sid) throws SQLException {
    SessionQuery sq = new SessionQuery(sid);
    query(sq);
    return sq.getUserID();
  }

  /**
   * Used to log the user one, by deleting the session information.
   * 
   * @param sid
   *          The session ID to be removed.
   * @return Whether or not the deleting was successful.
   * @throws SQLException
   *           Thrown when there is an error with the database interaction.
   */
  public boolean deleteSession(String sid) throws SQLException {
    SessionDelete sd = new SessionDelete(sid);
    int rowsChanged = update(sd);
    return rowsChanged == 1;
  }

  /**
   * Function to run the SQLUpdate on the database, used for insertion
   * operations.
   * 
   * @param insertion
   *          The query to be executed.
   * @return Whether or not the insertion was successful.
   * @throws SQLException
   *           Thrown when there is an error with the database interaction.
   */
  private boolean insert(SQLUpdate insertion) throws SQLException {
    Statement stmt;
    stmt = conn.createStatement();
    boolean rs = stmt.execute(insertion.getSQLUpdate());
    return rs;
  }

  /**
   * Function to run the SQLUpdate on the database, used for update/delete
   * operations.
   * 
   * @param query
   *          The query to be executed.
   * @return How many rows were affected by the update.
   * @throws SQLException
   *           Thrown when there is an error with the database interaction.
   */
  private int update(SQLUpdate query) throws SQLException {
    Statement stmt;
    stmt = conn.createStatement();
    int result = stmt.executeUpdate(query.getSQLUpdate());
    query.checkResult(result);
    return result;
  }

  /**
   * Function to run the SQLQuery on the database, used for querying operations.
   * 
   * This function returns results by setting them to fields within the SQLQuery
   * object that was passed in.
   * 
   * @param query
   *          The query to be executed.
   * @return Whether the query was successful.
   * @throws SQLException
   *           Thrown when there is an error with the database interaction.
   */
  private boolean query(SQLQuery query) throws SQLException {
    Statement stmt;
    stmt = conn.createStatement();
    ResultSet result = stmt.executeQuery(query.getSQLQuery());
    query.setResult(result);
    return true;
  }

  /**
   * Destroys the database connection used by this class
   * 
   * @return Whether the connection was closed normally.
   */
  public boolean destory() {
    try {
      conn.close();
    } catch (SQLException e) {
      return false;
    }
    return true;
  }
}
