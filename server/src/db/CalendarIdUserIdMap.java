package db;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import exception.InconsistentDataException;

import resp.CalendarResponse;
import resp.CalendarSubscriptionResponse;

import utils.ConcurrentHashSet;
import utils.DataSourceProvider;

public class CalendarIdUserIdMap {

  private static final DBInterface db = new DBInterface(
      DataSourceProvider.getSource());
  private static CalendarIdUserIdMap instance;

  /*
   * Map from calendar IDs to list (set) of user IDs subscribed to the certain
   * calendar.
   */
  private ConcurrentMap<Integer, ConcurrentHashSet<Integer>> map;

  private CalendarIdUserIdMap() {
    map = new ConcurrentHashMap<Integer, ConcurrentHashSet<Integer>>();
  }

  public static synchronized CalendarIdUserIdMap getInstance() {
    if (instance == null) {
      instance = new CalendarIdUserIdMap();
    }
    return instance;
  }

  public void put(Integer calendarId, Integer userId) {
    if (!map.containsKey(calendarId)) {
      map.put(calendarId, new ConcurrentHashSet<Integer>());
    }
    map.get(calendarId).add(userId);
  }

  /**
   * Given the calendar ID, return the array of calendar subscribers.
   * 
   * @param calendar
   *          ID of the queried calendar
   * @return the array of user IDs (users subscribed to a given calendar)
   */
  public Integer[] getUserIds(Integer calendarId) {
    ConcurrentHashSet<Integer> set = map.get(calendarId);
    if (set != null) {
      Object[] userIds = set.toArray();
      return (Arrays.copyOf(userIds, userIds.length, Integer[].class));
    }
    return null;
  }

  /**
   * Removes an entire calendar to user mapping given calendarId, useful when
   * the calendar is deleted
   * 
   * @param calendarId
   *          CalendarId to be removed from mapping
   */
  public void remove(Integer calendarId) {
    map.remove(calendarId);
  }

  /**
   * Given the calendar ID and the user ID, remove the mapping from the map.
   * 
   * @param calendarId
   * @param userId
   */
  public void remove(Integer calendarId, Integer userId) {
    ConcurrentHashSet<Integer> set = map.get(calendarId);
    if (set != null) {
      set.remove(userId);
      // Remove the mapping for calendar ID if no online user is subscribed to
      // that calendar.
      if (set.isEmpty()) {
        map.remove(calendarId);
      }
    }
  }

  /**
   * Called when the user logs out to remove his calendar subscriptions from the
   * map.
   * 
   * @param userId
   *          user Id of the user which logs out form the system
   * @return Set of calendar Ids that the user was subscribed to
   */
  public Set<Integer> deleteUser(int userId) {
    Set<Integer> calendarIds = new HashSet<Integer>();
    try {
      CalendarSubscriptionResponse resp = db.getUsersCalendars(userId);
      for (CalendarResponse calendar : resp.getCalendars()) {
        int calendarId = calendar.getCalendarId();
        calendarIds.add(calendarId);
        ConcurrentHashSet<Integer> set = map.get(calendarId);
        if (set != null) {
          set.remove(userId);
          /*
           * Remove the mapping for calendar ID if no online user is subscribed
           * to that calendar.
           */
          if (set.isEmpty()) {
            map.remove(calendarId);
          }
        }
      }
    } catch (SQLException | InconsistentDataException e) {
      System.err.println("Error while removing data from the map.");
    }
    return calendarIds;
  }

}
