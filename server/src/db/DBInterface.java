package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.postgresql.ds.PGConnectionPoolDataSource;

import req.CalendarRequest;
import req.CalendarRequest.CalendarEventsQuery;
import req.EventRequest;
import req.RegisterRequest;
import req.SessionRequest;
import req.UserRequest;
import resp.CalendarAuthResponse;
import resp.CalendarResponse;
import resp.CalendarSubscriptionResponse;
import resp.EventAdminResponse;
import resp.EventResponse;
import resp.EventSubscriptionResponse;
import resp.MessageResponse;
import resp.Response;
import resp.RosterResponse;
import resp.SavedEventResponse;
import resp.SessionResponse;
import resp.UserResponse;
import resp.ValidationResponse;
import sql.SQLDelete;
import sql.SQLInsert;
import sql.SQLQuery;
import sql.SQLUpdate;
import utils.AuthLevel;
import utils.CalendarIdQuery;
import utils.CalendarJoinCodeIdQuery;
import utils.CalendarJoinEnabledQuery;
import utils.EventEndTimeQuery;
import utils.EventStatus;
import utils.PasswordUtils;
import chat.ChatMessage;
import exception.CalendarNotFoundException;
import exception.CalendarSubscriptionNotFoundException;
import exception.EventNotFoundException;
import exception.InconsistentDataException;
import exception.InvalidActionException;
import exception.PasswordHashFailureException;
import exception.SessionNotFoundException;
import exception.UserNotFoundException;

/**
 * Class to manage the back-end to the database, deals with any and all queries.
 * 
 * @author bs2113
 * 
 */
public class DBInterface {

  private PGConnectionPoolDataSource source;

  /**
   * Builds the interface with the given data source.
   * 
   * @param source
   *          The data source used to obtain a database connection.
   */
  public DBInterface(PGConnectionPoolDataSource source) {
    this.source = source;
  }

  /**
   * Retrieve user entity from database.
   * 
   * @param uq
   *          User request containing the information to look up the user.
   * @return A UserResponse with the email,password and ID of the user.
   * @throws SQLException
   *           There was an error in the database.
   * @throws UserNotFoundException
   *           Thrown if the users data was not in the database.
   * @throws InconsistentDataException
   *           Thrown when the database is shown to be in a bad inconsistent
   *           state.
   */
  public UserResponse getUser(UserRequest uq) throws SQLException,
      UserNotFoundException, InconsistentDataException {

    UserResponse rs = new UserResponse(uq.getEmail(), uq.getPassword(),
        uq.getUserId(), null, null);

    query(rs);
    rs.checkResult();
    return rs;
  }

  public UserResponse getUser(String email) throws SQLException,
      UserNotFoundException, InconsistentDataException {
    return getUser(new UserRequest(email, null));
  }

  /**
   * Retrieve session information from a supplied session ID number. Used to
   * find out the user id, which user is logged for rights, etc.
   * 
   * @param sid
   *          The ID of the session to be looked up.
   * @return The session response with user id.
   * @throws SQLException
   *           Thrown when there is an error with the database interaction.
   */
  public SessionResponse getSession(String sid) throws SQLException,
      SessionNotFoundException {
    SessionResponse sr = new SessionResponse(sid);
    query(sr);
    if (!sr.isFound()) {
      throw new SessionNotFoundException("The Session could not be found");
    }
    return sr;
  }

  /**
   * Retrieve a calendar by its id.
   * 
   * @param calendarId
   *          CalendarRequest
   * @return CalendarResponse
   * @throws SQLException
   */
  public CalendarResponse getCalendar(CalendarRequest request)
      throws SQLException {
    CalendarResponse result = new CalendarResponse(request.getCalendarId(),
        request.getUserId());
    query(result);

    // Role based authentication
    if (result.getRole() == AuthLevel.NONE) {
      return CalendarResponse.NO_CALENDAR;
    }

    if (request.getStartDate() != null) {
      EventStatus status = EventStatus.ACTIVE;
      if (result.getRole() == AuthLevel.ADMIN) {
        status = EventStatus.PENDING;
      }
      CalendarEventsQuery query = request.getCalendarEventsQuery(status);
      query(query);
      result.setEvents(query.getEvents());
    }
    return result;
  }

  /**
   * Given the join code of a calendar and the ID of a user, register user's
   * subscription to a given calendar.
   * 
   * @param SubscriptionRequest
   *          containing the user ID and calendar join code
   * @return CalendarResponse
   * @throws SQLException
   */
  public CalendarResponse putCalendarSubscription(int userId, String joinCode)
      throws SQLException {
    CalendarSubscriptionResponse subResp = new CalendarSubscriptionResponse(
        userId, joinCode);
    insert(subResp);
    CalendarResponse resp = getCalendar(new CalendarRequest(userId,
        getCalendarId(new CalendarJoinCodeIdQuery(joinCode))));
    return resp;
  }

  /**
   * Deletes a calendar subscription given an userId and a calendarId
   * 
   * @param userId
   * @param calendarId
   * @return true on success, throws exception otherwise
   * @throws SQLException
   * @throws InconsistentDataException
   * @throws CalendarSubscriptionNotFoundException
   */
  public boolean deleteCalendarSubscription(int userId, int calendarId)
      throws SQLException, InconsistentDataException,
      CalendarSubscriptionNotFoundException {
    CalendarSubscriptionResponse response = new CalendarSubscriptionResponse(
        userId, calendarId, null);
    int rows = delete(response);
    if (rows == 1) {
      return true;
    } else if (rows == 0) {
      throw new CalendarSubscriptionNotFoundException(
          "Calendar subscription not found for " + userId + '-' + calendarId);
    }
    throw new InconsistentDataException(
        "Update calendar subscription role modified more than 1 row!");
  }

  /**
   * Get all events to which the user subscribed.
   * 
   * @throws SQLException
   * @throws InconsistentDataException
   */
  public CalendarSubscriptionResponse getUsersCalendars(int userId)
      throws SQLException, InconsistentDataException {
    List<CalendarResponse> cals = new ArrayList<>();
    CalendarSubscriptionResponse resp = new CalendarSubscriptionResponse(userId);
    Connection conn = source.getConnection();
    try {
      query(resp);
      ResultSet result = resp.getResultSet();
      while (result.next()) {
        CalendarResponse calendar = getCalendar(new CalendarRequest(userId,
            result.getInt(CalendarSubscriptionResponse.CID_COLUMN)));

        // User is subscribed to a calendar that he doesn't have access to
        if (calendar == CalendarResponse.NO_CALENDAR) {
          throw new InconsistentDataException(
              "User is no longer subscribed to this calendar.");
        }

        // Remove join code if user is not an admin
        if (calendar.getRole() == AuthLevel.BASIC) {
          calendar.setJoinEnabled(null);
          calendar.setJoinCode(null);
        }

        cals.add(calendar);
      }
    } finally {
      conn.close();
    }
    resp.setCalendars(cals);
    return resp;
  }

  /**
   * Store user entity into database.
   * 
   * @param rq
   *          RegisterRequest that contains user registration information
   * @return userId primary key generated by database
   * @throws SQLException
   *           if insertion failed
   * @throws PasswordHashFailureException
   */
  public int putUser(RegisterRequest rq, String validationCode)
      throws SQLException, PasswordHashFailureException {

    // Get the password in and put it in the pass variable
    UserResponse us = new UserResponse(rq.getEmail(),
        PasswordUtils.getPasswordHash(rq.getPassword()),
        UserResponse.INVALID_USER_ID, rq.getFirstName(), rq.getLastName());
    us.setValidationCode(validationCode);
    return insert(us, true, UserResponse.ID_COLUMN);
  }

  /**
   * Store a new session to the database, called within the SessionManager
   * class.
   * 
   * @param sq
   *          The SessionRequest that is to be added.
   * @return Whether or not the insertion was a success.
   * @throws SQLException
   *           Thrown when there is a problem with the database interaction.
   */
  public boolean putSession(SessionRequest sq) throws SQLException {
    SessionResponse sr = new SessionResponse(sq.getSessionId(), sq.getUserId());
    return insert(sr) == 1;
  }

  /**
   * Store the calendar into the database.
   * 
   * @param cr
   *          CalendarRequest object submitted by client
   * @return CalendarResponse object
   * @throws SQLException
   */
  public CalendarResponse putCalendar(CalendarRequest cq) throws SQLException {
    CalendarResponse cr = new CalendarResponse(cq.getName(),
        cq.isJoinEnabled(), cq.getUserId(), cq.getInviteCode());
    cr.setCalendarID(insert(cr, true, CalendarResponse.CID_COLUMN));
    return cr;
  }

  /**
   * Deletes a specified calendar from the database given the calendarId
   * 
   * @param calendarId
   * @return CalendarResponse from the database that is deleted
   * @throws SQLException
   * @throws InconsistentDataException
   * @throws CalendarNotFoundException
   */
  public CalendarResponse deleteCalendar(int calendarId) throws SQLException,
      InconsistentDataException, CalendarNotFoundException {
    CalendarResponse cr = new CalendarResponse(calendarId, false);
    int deletedRows = update(cr);
    if (deletedRows > 1) {
      throw new InconsistentDataException("More than one calendar was deleted.");
    }
    if (deletedRows == 0) {
      throw new CalendarNotFoundException(
          "No calendar with the specified ID was found.");
    }
    return cr;
  }

  /**
   * Store the event into the database.
   * 
   * @param er
   *          EventRequest object submitted by client
   * @return event ID
   * @throws SQLException
   */
  public EventResponse putEvent(EventRequest ereq, EventStatus status, int userId) throws SQLException {
    // TODO: why convert max to string?
    EventResponse eresp = new EventResponse(ereq.getTitle(),
        ereq.getDescription(), ereq.getLocation(), ereq.getStartDateTime(),
        ereq.getEndDateTime(), Integer.toString(ereq.getMax()), -1,
        ereq.getCalendarId(), status, userId);
    int id = insert(eresp, true, EventResponse.EID_COLUMN);
    eresp.setEventId(id);
    return eresp;
  }

  /**
   * Adds an event subscription.
   * 
   * @param eventId
   *          - ID of the event to which a user subscribes
   * @param userId
   *          - ID of the user who wants
   * 
   * @return Whether the insertion was successful or not.
   * @throws SQLException
   *           Thrown when there was an error interacting with the database.
   * @throws InvalidActionException
   */
  public EventSubscriptionResponse putEventSubscription(int eventId, int userId)
      throws SQLException, InvalidActionException {
    // Untested
    EventSubscriptionResponse response = new EventSubscriptionResponse(eventId,
        userId);
    if (!(insert(response) == 1)) {
      throw new InvalidActionException("Tried to join a full event");
    }
    return response;
  }

  /**
   * Gets the list of events that a user has subscribed to.
   * 
   * @param esr
   *          The request object of the event in question (only the event ID is
   *          needed).
   * @return An EventSubscriptionResponse, you can get the list of users by
   *         calling getSubscriberList() on this.
   * @throws SQLException
   *           Thrown where there was an error when interacting with the
   *           database.
   * @throws InconsistentDataException
   *           Thrown when the data in the database is shown to be impossible
   * @throws UserNotFoundException
   *           Thrown if the user was deleted between the issuing of the request
   *           and the response
   */
  public EventSubscriptionResponse getEventSubscription(int userId)
      throws SQLException {
    // Get event subscription
    EventSubscriptionResponse eventSubs = new EventSubscriptionResponse(userId);
    query(eventSubs);

    // Retrieve event details from ids
    List<Integer> eventIds = eventSubs.getJoinedEventIds();
    for (int eid : eventIds) {
      EventResponse event = new EventResponse(eid);
      query(event);
      eventSubs.addEvent(event);
    }

    return eventSubs;
  }

  /**
   * Given an event id, returns the list of attendees, should only be called by
   * admins of this event.
   * 
   * @param eventId
   * @return
   * @throws SQLException
   * @throws UserNotFoundException
   * @throws InconsistentDataException
   */
  public EventAdminResponse getEventAttendees(int eventId) throws SQLException,
      UserNotFoundException, InconsistentDataException {
    List<UserResponse> userResponses = new ArrayList<>();
    EventAdminResponse response = new EventAdminResponse(eventId);

    query(response);

    // Retrieve user details based on user id
    List<Integer> users = response.getAttendeeIds();
    for (Integer user : users) {
      userResponses.add(getUser(new UserRequest(user)));
    }
    response.setAttendees(userResponses);

    return response;
  }

  /**
   * Deletes an event subscription.
   * 
   * @param esr
   *          The request object of the event to be deleted.
   * @return (0) if the event subscription was not found or (1) if it was
   *         successfully deleted.
   * @throws SQLException
   * @throws InconsistentDataException
   */
  public boolean deleteEventSubscription(int eventId, int userId)
      throws SQLException, InconsistentDataException {
    EventSubscriptionResponse response = new EventSubscriptionResponse(eventId,
        userId);
    int rows = delete(response);
    if (rows > 1) {
      throw new InconsistentDataException(
          "Deleting an event subscription removed more than one row");
    }
    return rows == 1;
  }

  /**
   * Updates users role in the USER_CALENDAR table
   * 
   * @param targetUserId
   * @param calendarId
   * @param role
   * @return true on success, throws exception otherwise
   * @throws CalendarSubscriptionNotFoundException
   * @throws InconsistentDataException
   * @throws SQLException
   */
  public boolean updateUserRole(int targetUserId, int calendarId, AuthLevel role)
      throws CalendarSubscriptionNotFoundException, InconsistentDataException,
      SQLException {
    CalendarSubscriptionResponse resp = new CalendarSubscriptionResponse(
        targetUserId, calendarId, role);
    int rows = update(resp);
    if (rows == 1) {
      return true;
    } else if (rows == 0) {
      throw new CalendarSubscriptionNotFoundException(
          "Calendar subscription not found for " + targetUserId + '-'
              + calendarId);
    }
    throw new InconsistentDataException(
        "Update calendar subscription role modified more than 1 row!");
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
  private boolean updateUser(UserResponse ur) throws SQLException,
      InconsistentDataException, UserNotFoundException {
    int rows = update(ur);
    if (rows == 1) {
      return true;
    } else if (rows == 0) {
      throw new UserNotFoundException(
          "The user could not be updated, as they don't exist");
    }
    throw new InconsistentDataException(
        "Update user info modified more than 1 row!");
  }

  public boolean updateUser(int userId, RegisterRequest rr)
      throws SQLException, InconsistentDataException, UserNotFoundException {
    return updateUser(new UserResponse(rr.getEmail(), rr.getPassword(), userId,
        rr.getFirstName(), rr.getLastName()));
  }

  public boolean updateUser(int userId, String validationCode)
      throws SQLException, InconsistentDataException, UserNotFoundException {
    return updateUser(new UserResponse(userId, validationCode));
  }

  public boolean updateUser(String email, String newPassword)
      throws SQLException, InconsistentDataException, UserNotFoundException {
    return updateUser(new UserResponse(email, newPassword,
        UserResponse.INVALID_USER_ID, null, null));
  }

  /**
   * Functions to update event details for a given event Id.
   * 
   * @param eventId
   *          The id of the event which needs to be updated.
   * @param ereq
   *          The request containing the information that need to be put in.
   *          nulls indicate that the values will not change.
   * @return EventResponse which is not null if the update was successful.
   * @throws SQLException
   *           Thrown when a database error occurs.
   * @throws EventNotFoundException
   *           Thrown when the event could not be found in the database.
   * @throws InconsistentDataException
   *           Thrown when the database is shown to be in a inconsistent state.
   *           This MUST be dealt with.
   */
  public EventResponse updateEvent(int eventId, EventRequest ereq, int userId)
      throws SQLException, EventNotFoundException, InconsistentDataException {
    // TODO: why convert max to string?
    EventResponse er = new EventResponse(ereq.getTitle(),
        ereq.getDescription(), ereq.getLocation(), ereq.getStartDateTime(),
        ereq.getEndDateTime(), Integer.toString(ereq.getMax()), eventId, -1, null, userId);
    return updateRowCheckHelper(er);
  }

  /**
   * Updates calendar details given a calendarId and a request
   * 
   * @param calendarId
   * @param creq
   * @return null if request is null, CalendarResponse of the updated event
   *         otherwise
   * @throws SQLException
   * @throws InconsistentDataException
   * @throws CalendarNotFoundException
   */
  public CalendarResponse updateCalendar(int calendarId, CalendarRequest creq)
      throws SQLException, InconsistentDataException, CalendarNotFoundException {
    if (creq == null) {
      return null;
    }

    CalendarResponse cr = new CalendarResponse(calendarId, creq.getName(),
        creq.isJoinEnabled(), creq.getInviteCode());
    int rows = update(cr);
    if (rows > 1) {
      throw new InconsistentDataException(
          "Calendar update affected more than one calendar.");
    }
    if (rows == 0) {
      throw new CalendarNotFoundException(
          "No calendar with the specified ID was found.");
    }
    return cr;
  }

  /**
   * Deletes an event (marks as inactive) from (in) the database.
   * 
   * @param eventId
   *          Event to be marked
   * @return EventResponse which is not null if the deletion was successful
   * @throws EventNotFoundException
   *           When the eventId given was not found in the database
   * @throws InconsistentDataException
   *           When the deletion modified more than one row. This should never
   *           happen.
   * @throws SQLException
   *           Thrown when there is a error in interacting with the database
   */
  public EventResponse deleteEvent(int eventId) throws EventNotFoundException,
      InconsistentDataException, SQLException {
    EventResponse er = new EventResponse(eventId, EventStatus.DELETED);
    return updateRowCheckHelper(er);
  }

  /**
   * Helper to perform the update for a response /w a row check This should only
   * be used when you only expect ONE row to be changed by the query.
   * 
   * @param r
   *          Response representing the query to be run.
   * @return Response corresponding to the given update
   * @throws EventNotFoundException
   *           See delete or update event.
   * @throws InconsistentDataException
   *           See delete or update event.
   * @throws SQLException
   *           See delete or update event.
   */
  private EventResponse updateRowCheckHelper(EventResponse r)
      throws EventNotFoundException, InconsistentDataException, SQLException {
    int rows = update(r);
    if (rows == 1) {
      return r;
    }
    if (rows == 0) {
      throw new EventNotFoundException(
          "The event could not be altered, as it doesn't exist");
    }
    throw new InconsistentDataException(
        "Altering event info modified more than 1 row!");
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
    SessionResponse sr = new SessionResponse(sid, UserResponse.INVALID_USER_ID);
    int rowsChanged = delete(sr);
    // If this it not 1 we may have a problem and wish to log it/
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
  private int insert(SQLInsert insertion, boolean returnKey, String keyColumn)
      throws SQLException {
    Connection conn = source.getConnection();
    int ret = 0;
    try {
      PreparedStatement stmt = conn.prepareStatement(insertion.getSQLInsert(),
          Statement.RETURN_GENERATED_KEYS);
      insertion.formatSQLInsert(stmt);
      ret = stmt.executeUpdate();
      if (returnKey) {
        ResultSet rs = stmt.getGeneratedKeys();
        rs.next();
        ret = rs.getInt(keyColumn);
      }
    } finally {
      conn.close();
    }
    return ret;
  }

  private int insert(SQLInsert insertion) throws SQLException {
    return insert(insertion, false, null);
  }

  /**
   * Function to run the SQLUpdate on the database, used for update/delete
   * operations. Returns 1 if the query is skipped due to it having no effect.
   * 
   * @param query
   *          The query to be executed.
   * @return How many rows were affected by the update.
   * @throws SQLException
   *           Thrown when there is an error with the database interaction.
   */
  private int update(SQLUpdate update, String override) throws SQLException {
    Connection conn = source.getConnection();
    int result = 0;
    try {
      String q;
      if (override == null) {
        q = update.getSQLUpdate();
      } else {
        q = override;
      }
      if (q != null) {
        PreparedStatement stmt = conn.prepareStatement(q);
        if (override == null) {
          update.formatSQLUpdate(stmt);
        }
        result = stmt.executeUpdate();
        update.checkResult(result);
        return result;
      }
    } finally {
      conn.close();
    }
    // The query is pointless, return 1 to signal success
    return 1;
  }

  private int update(SQLUpdate update) throws SQLException {
    return update(update, null);
  }

  private int delete(SQLDelete delete) throws SQLException {
    Connection conn = source.getConnection();
    int result = 0;
    try {
      PreparedStatement stmt = conn.prepareStatement(delete.getSQLDelete());
      delete.formatSQLDelete(stmt);
      result = stmt.executeUpdate();
    } finally {
      conn.close();
    }
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
  private boolean query(SQLQuery query, String override) throws SQLException {
    Connection conn = source.getConnection();
    ResultSet result = null;
    try {
      String q;
      if (override == null) {
        q = query.getSQLQuery();
      } else {
        q = override;
      }
      PreparedStatement stmt = conn.prepareStatement(q);
      if (override == null) {
        query.formatSQLQuery(stmt);
      }
      result = stmt.executeQuery();
      query.setResult(result);
    } finally {
      conn.close();
    }
    return true;
  }

  private boolean query(SQLQuery query) throws SQLException {
    return query(query, null);
  }

  /**
   * Performs a database update to refresh a session.
   * 
   * @param userId
   *          The user whose session needs to be refreshed.
   * @return Whether the update was successful.
   * @throws SQLException
   *           Thrown when there is an error interacting with the database.
   */
  public boolean updateSession(int userId, String sessionId)
      throws SQLException {
    SessionResponse sr = new SessionResponse(sessionId, userId);
    int rows = update(sr, sr.getSQLRefresh());
    return rows == 1;
  }

  /**
   * Gets the permissions of a given user for a given calendar.
   * 
   * @param uid
   *          The id of the user to check.
   * @param cid
   *          The id of the calendar to check.
   * @return The AuthLevel of the user.
   */
  public AuthLevel authoriseUser(int uid, int cid) {
    CalendarAuthResponse car = new CalendarAuthResponse(uid, cid);
    try {
      query(car);
    } catch (SQLException e) {
      return AuthLevel.NONE;
    }
    return AuthLevel.getAuth(car.getAccessPrivilege());
  }

  /**
   * Gets a calendarId of the calendar in which the specified event was
   * published.
   * 
   * @param eventId
   *          of the queried event
   * @return calendarId of the corresponding calendar
   */
  public int getCalendarId(CalendarIdQuery query) {
    try {
      query(query);
    } catch (SQLException e) {
      return 0;
    }
    return query.getCalendarId();
  }

  /**
   * Has the specified event already happened.
   * 
   * @param eventId
   * @return boolean value indicating if the event has already happened
   * @throws SQLException
   */
  public boolean isPastEvent(int eventId) throws SQLException {
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    Date currentDate = calendar.getTime();
    EventEndTimeQuery query = new EventEndTimeQuery(eventId);
    query(query);
    return currentDate.getTime() > query.getEndTime().getTime();
  }

  public boolean checkValidation(String email, String validationCode)
      throws SQLException {
    ValidationResponse vr = new ValidationResponse(email, validationCode);
    update(vr);
    return vr.isValid();
  }

  /**
   * Is the specified calendar joinable.
   * 
   * @param calendarId
   * @return
   * @throws SQLException
   */
  public boolean isCalendarJoinable(int calendarId) throws SQLException {
    CalendarJoinEnabledQuery query = new CalendarJoinEnabledQuery(calendarId);
    query(query);
    return query.isJoinEnabled();
  }

  /**
   * Returns the details of the user that the given user ID can talk to
   * 
   * @param userId
   *          The id of the user to query for
   * @return The response object that contains the users details
   * @throws SQLException
   *           Thrown when there is an error in the database interaction
   */
  public RosterResponse getRoster(int userId) throws SQLException {
    RosterResponse rr = new RosterResponse(userId);
    query(rr);
    return rr;
  }

  /**
   * Inserts a chat message into the database, used for offline messages and
   * possibly logging?
   * 
   * @param ch
   *          The message to save
   * @throws SQLException
   *           Thrown when there is an error in the database interaction
   */
  public void insertMessage(ChatMessage ch, Integer destinationId)
      throws SQLException {
    MessageResponse mr = new MessageResponse(ch.getType(), ch.getSourceId(),
        destinationId, ch.getPayloadString());
    insert(mr);
  }

  /**
   * Gets the messages that need to be sent to a given user
   * 
   * @param userId
   *          The userId of who to get the messages for
   * @return A list of MessageResponses containing the required message data
   * @throws SQLException
   *           Thrown when there is an error in the database interaction
   */
  public List<ChatMessage> getMessages(int userId) throws SQLException {
    MessageResponse mr = new MessageResponse(userId);
    query(mr);
    return mr.getMessages();
  }

  /**
   * Given the ID of the user, returns the events that the user saved (together
   * with the appropriate timestamps).
   * 
   * @param userId
   * @return List of EventResponse objects and corresponding timestamps.
   * @throws SQLException
   */
  public SavedEventResponse getSavedEvents(int userId) throws SQLException {
    SavedEventResponse query = new SavedEventResponse(userId);
    query(query);
    return query;
  }

  /**
   * Saves the event with specified ID for the given user.
   * 
   * @param userId
   * @param eventId
   * @return whether the event was successfully saved
   * @throws SQLException
   */
  public boolean saveEvent(int userId, int eventId) throws SQLException,
      InconsistentDataException {
    SavedEventResponse resp = new SavedEventResponse(userId, eventId);
    int rowsUpdate = update(resp);
    if (rowsUpdate > 1) {
      throw new InconsistentDataException("Error while saving the event, "
          + "more than once timestamp was updated.");
    } else if (rowsUpdate == 1) {
      // Event was already saved, the time stamp was successfully updated.
      return true;
    } else {
      return insert(resp) == 1;
    }
  }

  /**
   * Given the ID of the event, it removes the event from the list of saved
   * events of the specified user.
   * 
   * @param userId
   * @param eventId
   * @return whether the deletion was successful
   * @throws SQLException
   * @throws InconsistentDataException
   *           - thrown when more than one row is deleted
   */
  public boolean deleteSavedEvent(int userId, int eventId) throws SQLException,
      InconsistentDataException {
    SavedEventResponse resp = new SavedEventResponse(userId, eventId);
    int rows = delete(resp);
    if (rows > 1) {
      throw new InconsistentDataException(
          "More than one event from the list of saved events was deleted.");
    }
    return rows == 1;
  }

  public boolean activateEvent(int eventId) throws SQLException,
      InconsistentDataException {
    EventResponse response = new EventResponse(eventId);
    //response.setActive();
    if (update(response) != 1) {
      throw new InconsistentDataException("Triggered in activateEvent");
    }
    return true;
  }

}
