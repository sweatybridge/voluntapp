package resp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RosterResponse extends Response {

  private List<RosterEntry> roster;

  private transient int userId;
  private transient ResultSet rs;
  private transient Set<Integer> lookup = new HashSet<Integer>();

  public class RosterEntry {
    private int uid;
    private int cid;
    private String firstName;
    private String lastName;
    private List<String> calNames = new ArrayList<String>();

    public RosterEntry(int uid, int cid, String firstName, String lastName) {
      this.uid = uid;
      this.cid = cid;
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public void addCalName(String name) {
      this.calNames.add(name);
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

  @Override
  public String getSQLQuery() {
    // First param UID
    return String
        .format(
            "SELECT \"%s\",\"%s\",\"%s\",\"%s\",\"%s\" FROM (SELECT \"%s\",\"%s\",\"%s\",\"%s\" "
                + "FROM (SELECT \"%s\", x.\"%s\" FROM (SELECT \"%s\" FROM \"USER_CALENDAR\" WHERE \"%s\"=?) AS x NATURAL JOIN "
                + "\"USER_CALENDAR\" AS y WHERE \"%s\"='owner' OR \"%s\"='editor' OR \"%s\"='admin') AS z JOIN \"USER\" ON \"USER\".\"%s\"=z.\"%s\") "
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
          rs.getInt(CalendarSubscriptionResponse.CID_COLUMN),
          rs.getString(UserResponse.FIRST_NAME_COLUMN),
          rs.getString(UserResponse.LAST_NAME_COLUMN));
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
