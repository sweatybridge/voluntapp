package utils;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import exception.PasswordHashFailureException;

/**
 * Modified from example at
 * http://howtodoinjava.com/2013/07/22/how-to-generate-secure
 * -password-hash-md5-sha-pbkdf2-bcrypt-examples/
 * 
 * @author bs2113
 * 
 */
public class PasswordUtils {

  private static final int iter = 20 * 1000;
  private static final int saltLen = 32;
  private static final int keyLen = 256;

  public static String getPasswordHash(String plainPass)
      throws PasswordHashFailureException {

    char[] chars = plainPass.toCharArray();
    byte[] salt = getSalt();
    PBEKeySpec spec = new PBEKeySpec(chars, salt, iter, (keyLen / 2) * 8);
    SecretKeyFactory skf;
    try {
      skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    } catch (NoSuchAlgorithmException e1) {
      throw new PasswordHashFailureException(
          "Password hashing failed, reason: " + e1.getMessage());
    }
    byte[] hash;
    try {
      hash = skf.generateSecret(spec).getEncoded();
    } catch (InvalidKeySpecException e) {
      throw new PasswordHashFailureException(
          "Password hashing failed, reason: " + e.getMessage());
    }
    return toHex(salt) + ":" + toHex(hash);
  }

  private static byte[] getSalt() throws PasswordHashFailureException {
    byte[] salt = new byte[saltLen];
    SecureRandom sr;
    try {
      try {
        sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
      } catch (NoSuchProviderException e) {
        sr = SecureRandom.getInstance("SHA1PRNG");
      }
    } catch (NoSuchAlgorithmException e) {
      throw new PasswordHashFailureException("Salt Generation failed, reason: "
          + e.getMessage());
    }
    sr.nextBytes(salt);
    return salt;
  }

  private static String toHex(byte[] array) {
    BigInteger bi = new BigInteger(1, array);
    String hex = bi.toString(16);
    int paddingLength = (array.length * 2) - hex.length();
    if (paddingLength > 0) {
      return String.format("%0" + paddingLength + "d", 0) + hex;
    } else {
      return hex;
    }
  }

  public static boolean validatePassword(String givenPassword,
      String storedPassword) throws PasswordHashFailureException {
    String[] parts = storedPassword.split(":");
    System.out.println(parts[0]);
    System.out.println(parts[1]);
    byte[] salt = fromHex(parts[0]);
    byte[] hash = fromHex(parts[1]);

    PBEKeySpec spec = new PBEKeySpec(givenPassword.toCharArray(), salt, iter,
        hash.length * 8);
    SecretKeyFactory skf;
    try {
      skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    } catch (NoSuchAlgorithmException e) {
      throw new PasswordHashFailureException(
          "Password hashing failed, reason: " + e.getMessage());
    }
    byte[] testHash;
    try {
      testHash = skf.generateSecret(spec).getEncoded();
    } catch (InvalidKeySpecException e) {
      throw new PasswordHashFailureException(
          "Password hashing failed, reason: " + e.getMessage());
    }

    int diff = hash.length ^ testHash.length;
    for (int i = 0; i < hash.length && i < testHash.length; i++) {
      diff |= hash[i] ^ testHash[i];
    }
    return diff == 0;
  }

  private static byte[] fromHex(String hex) {
    byte[] bytes = new byte[hex.length() / 2];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
    }
    return bytes;
  }

}
