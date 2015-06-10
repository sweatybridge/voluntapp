package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class for holding the formats of the chat Roster, used
 * when the user signs onto the system to send the list of all
 * available people they can chat to. 
 * 
 * @author bs2113
 *
 */
/**
 * @author bs2113
 * 
 */
public class RosterResponse extends Response {

  // List of the people in the roster (JSON target)
  private List<RosterEntry> roster;

  // Internal fields not serialized by JSON
  private transient int userId;
  private transient ResultSet rs;
  private transient Set<Integer> lookup = new HashSet<Integer>();

  /**
   * Anonymous class to store each of the users in the roster.
   * 
   * @author bs2113
   * 
   */
  public class RosterEntry {
    private int uid;
    private List<Integer> cids;
    private String firstName;
    private String lastName;
    private List<String> calNames = new ArrayList<String>();
    private boolean isOnline = false;

    public RosterEntry(int uid, String firstName, String lastName) {
      this.uid = uid;
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public void addCalName(String name) {
      this.calNames.add(name);
    }

    public void addcid(Integer cid) {
      this.cids.add(cid);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof RosterEntry)) {
        if (o instanceof Integer) {
          return (Integer) o == this.getuid();
        }
        return false;
      }
      RosterEntry re = (RosterEntry) o;
      return this.getuid() == re.getuid();
    }

    public int getuid() {
      return uid;
    }
    
    public int getcid() {
      return cid;
    }
    
    public void setIsOnline(boolean isOnline) {
      this.isOnline = isOnline;
    }
  }

  /**
   * Empty constructor for gson
   */
  public RosterResponse() {
  }

  public RosterResponse(int userId) {
    this.roster = new ArrayList<RosterEntry>();
    this.userId = userId;
  }

  public int getUserId() {
    return userId;
  }
  
  public List<RosterEntry> getRosterEntries() {
    return roster;
  }

  @Override
  public String getSQLQuery() {
    // First param UID
    return String
        .format(
            "SELECT \"%s\",\"%s\",\"%s\",\"%s\",\"%s\" FROM (SELECT \"%s\",\"%s\",\"%s\",\"%s\" "
                + "FROM (SELECT \"%s\", x.\"%s\" FROM (SELECT \"%s\" FROM \"USER_CALENDAR\" WHERE \"%s\"=?) AS x NATURAL JOIN "
                + "\"USER_CALENDAR\" AS y WHERE \"%s\"='owner' OR \"%s\"='editor' OR \"%s\"='admin' OR \"%s\"='basic') AS z JOIN \"USER\" ON \"USER\".\"%s\"=z.\"%s\") "
                + "AS a JOIN \"CALENDAR\" ON a.\"%s\"=\"CALENDAR\".\"%s\" WHERE \"CALENDAR\".\"%s\"=true;",
            CalendarSubscriptionResponse.UID_COLUMN,
            CalendarSubscriptionResponse.CID_COLUMN,
            UserResponse.FIRST_NAME_COLUMN, UserResponse.LAST_NAME_COLUMN,
            CalendarResponse.CNAME_COLUMN,
            CalendarSubscriptionResponse.UID_COLUMN,
            CalendarSubscriptionResponse.CID_COLUMN,
            UserResponse.FIRST_NAME_COLUMN, UserResponse.LAST_NAME_COLUMN,
            CalendarSubscriptionResponse.UID_COLUMN,
            CalendarSubscriptionResponse.CID_COLUMN,
            CalendarSubscriptionResponse.CID_COLUMN,
            CalendarSubscriptionResponse.UID_COLUMN,
            CalendarSubscriptionResponse.ROLE_COLUMN,
            CalendarSubscriptionResponse.ROLE_COLUMN,
            CalendarSubscriptionResponse.ROLE_COLUMN,
            CalendarSubscriptionResponse.ROLE_COLUMN, UserResponse.ID_COLUMN,
            CalendarSubscriptionResponse.UID_COLUMN,
            CalendarSubscriptionResponse.CID_COLUMN,
            CalendarResponse.CID_COLUMN, CalendarResponse.ACTIVE_COLUMN);
  }

  @Override
  public void formatSQLQuery(PreparedStatement prepared) throws SQLException {
    int i = 1;
    prepared.setInt(i++, userId);
  }

  private void processRow(ResultSet rs) throws SQLException {
    Integer userId = rs.getInt(CalendarSubscriptionResponse.UID_COLUMN);
    String calName = rs.getString(CalendarResponse.CNAME_COLUMN);
    if (!lookup.contains(userId)) {
      lookup.add(userId);
      RosterEntry re = new RosterEntry(userId,
          rs.getString(UserResponse.FIRST_NAME_COLUMN),
          rs.getString(UserResponse.LAST_NAME_COLUMN));
      re.addcid(rs.getInt(CalendarSubscriptionResponse.CID_COLUMN));
      re.addCalName(calName);
      roster.add(re);
    } else {
      for (RosterEntry i : roster) {
        if (i.equals(userId)) {
          i.addCalName(calName);
        }
      }
    }

  }

  @Override
  public void setResult(ResultSet result) {
    this.rs = result;
    try {
      while (rs.next()) {
        processRow(rs);
      }
    } catch (SQLException e) {
      return;
    }
  }
}
