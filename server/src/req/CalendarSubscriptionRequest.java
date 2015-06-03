package req;

import org.apache.commons.lang3.StringUtils;

import utils.AuthLevel;

import com.google.common.annotations.VisibleForTesting;

import db.CodeGenerator;

public class CalendarSubscriptionRequest implements Request {

  private String joinCode;
  private int targetUserEmail;
  private AuthLevel role;

  /**
   * Fields excluded from deserialisation.
   */
  private transient int userId;

  /**
   * No-arg constructor for compatibility with gson serialiser.
   */
  public CalendarSubscriptionRequest() {
  }

  @Override
  public boolean isValid() {
    return joinCode != null && joinCode.length() == CodeGenerator.CODE_LENGTH
        && StringUtils.isAlphanumeric(joinCode);
  }

  public String getJoinCode() {
    return joinCode;
  }
  
  public int getTargetUserId() {
    return targetUserEmail;
  }

  public AuthLevel getRole() {
    return role;
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
