package db;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import exception.InconsistentDataException;

import resp.CalendarResponse;
import resp.CalendarSubscriptionResponse;

import utils.ConcurrentHashSet;

public class CalendarIdUserIdMap {
  
  private DBInterface db;
  private static CalendarIdUserIdMap instance;
  
  /* Map from calendar IDs to list (set) of user IDs subscribed to the certain 
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
   * @param  calendar ID of the queried calendar 
   * @return the array of user IDs (users subscribed to a given calendar)
   */
  public Integer[] getUserIds(Integer calendarId) {
    ConcurrentHashSet<Integer> set = map.get(calendarId);
    if (set != null) {
      return ((Integer[]) set.toArray());
    }
    return null;
  }
  
  /**
   * Given the calnedar ID and the user ID, remove the mapping from the map.
   * 
   * @param calendarId
   * @param userId
   */
  public void remove(Integer calendarId, Integer userId) {
    ConcurrentHashSet<Integer> set = map.get(calendarId);
    if (set != null) {
      set.remove(userId);
      /* Remove the mapping for calendar ID if no online user is subscribed
       * to that calendar. */
      if (set.isEmpty()) {
        map.remove(calendarId);
      }
    }
  }
  
  /** 
   * Called when the user logs out to remove his calendar subscriptions from
   * the map.
   * 
   * @param user ID of the user which logs out form the system
   */
  public boolean deleteUser(int userId) {
    try {
      CalendarSubscriptionResponse resp = db.getUsersCalendars(userId);
      for (CalendarResponse calendar : resp.getCalendars()) {
        int calendarId = calendar.getCalendarId();
        ConcurrentHashSet<Integer> set = map.get(calendarId);
        if (set != null) {
          set.remove(userId);
          /* Remove the mapping for calendar ID if no online user is subscribed
           * to that calendar. */
          if (set.isEmpty()) {
            map.remove(calendarId);
          }
        }
        return true;
      }
    } catch (SQLException | InconsistentDataException e) {
      System.err.println("Error while removing data from the map.");
    }
    return false;
  }
  
}
