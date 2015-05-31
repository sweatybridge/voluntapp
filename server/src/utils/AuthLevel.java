package utils;

public enum AuthLevel {
  BASIC, EDITOR, ADMIN, NONE;

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
