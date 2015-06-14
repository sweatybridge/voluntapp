package utils;

/**
 * Enum representing the authorisation level for calendars.
 * 
 * @author bs2113
 * 
 */
public enum AuthLevel {
  NONE, BASIC, EDITOR, ADMIN;

  /**
   * Generates an enum from the given role string.
   * 
   * @param role
   *          Role string that should be mapped to AuthLevel
   * @return Corresponding AuthLevel, NONE if not recognised
   */
  public static AuthLevel getAuth(String role) {
    if (role.equalsIgnoreCase("editor")) {
      return AuthLevel.EDITOR;
    } else if (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("owner")) {
      return AuthLevel.ADMIN;
    } else if (role.equalsIgnoreCase("basic")) {
      return AuthLevel.BASIC;
    } else {
      return AuthLevel.NONE;
    }
  }

}
