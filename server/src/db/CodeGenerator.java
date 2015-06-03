package db;

import java.security.SecureRandom;
import java.util.Random;

public final class CodeGenerator {

  private final Random random;
  public static final int CODE_LENGTH = 6;

  private String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

  public CodeGenerator() {
    random = new SecureRandom();
  }

  public String getCode() {
    return getCode(6);
  }

  public synchronized String getCode(Integer length) {
    String res;
    do {
      res = "";
      for (int i = 0; i < length; i++) {
        int index = (int) (random.nextDouble() * letters.length());
        res += letters.substring(index, index + 1);
      }
    } while (taken(res));
    return res;
  }

  /* Function checking if the invite code is already in use. */
  private boolean taken(String inviteCode) {
    // TODO: implement this properly by making queries to the database.
    return false;
  }
}
