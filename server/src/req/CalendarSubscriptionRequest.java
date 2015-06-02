package req;

import org.apache.commons.lang3.StringUtils;

import com.google.common.annotations.VisibleForTesting;

import db.InviteCodeGenerator;

public class CalendarSubscriptionRequest implements Request {

  private String joinCode;

  /**
   * Fields excluded from deserialisation.
   */
  private transient int userId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public CalendarSubscriptionRequest() {}

  @Override
  public boolean isValid() {
    return joinCode != null
        && joinCode.length() == InviteCodeGenerator.CODE_LENGTH
        && StringUtils.isAlphanumeric(joinCode);
  }

  public String getJoinCode() {
    return joinCode;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  @VisibleForTesting
  protected void setJoinCode(String joinCode) {
    this.joinCode = joinCode;
  }
}
