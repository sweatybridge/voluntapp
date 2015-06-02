package db;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public final class InviteCodeGenerator {

  private final Random random;
  public static final int CODE_LENGTH = 6;

  public InviteCodeGenerator() {
    random = new SecureRandom();
  }

  public String getInviteCode() {
    String res = new BigInteger(30, random).toString(32);
    while (taken(res)) {
      res = align(new BigInteger(30, random).toString(32));
    }
    return res;
  }

  /* Aligns the invite code to a specific length. */
  private String align(String s) {
    char[] prefix = new char[CODE_LENGTH - s.length()];
    Arrays.fill(prefix, '0');
    return (prefix.toString() + s);
  }

  /* Function checking if the invite code is already in use. */
  private boolean taken(String inviteCode) {
    // TODO: implement this properly by making queries to the database.
    return false;
  }
}
