package db;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public final class CodeGenerator {

  private final Random random;
  public static final int CODE_LENGTH = 6;

  public CodeGenerator() {
    random = new SecureRandom();
  }

  public String getCode() {
    return getCode(6);
  }

  public String getCode(Integer length) {
    String res;
    do {
      res = align(new BigInteger(length * 5, random).toString(32), length);
    } while (taken(res));
    return res;
  }

  /* Aligns the invite code to a specific length. */
  private String align(String s, Integer length) {
    char[] prefix = new char[length - s.length()];
    Arrays.fill(prefix, '0');
    return (prefix.toString() + s);
  }

  /* Function checking if the invite code is already in use. */
  private boolean taken(String inviteCode) {
    // TODO: implement this properly by making queries to the database.
    return false;
  }
}
