package utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import exception.PasswordHashFailureException;

public class PasswordUtilsTest {

  private final List<String> TEST_DATA = Arrays.asList("Hello", "password",
      "qwerty", "12345678",
      "fdfmdkfmk43434213fsdfe6ytofkewmasder2345;;;[.;.'[p");
  private final String TEST_FAIL_STRING = "Hahahsahsahs";

  @Test
  public void doesHashingAndCheckingSucceedsCorrectly() {
    for (String s : TEST_DATA) {
      try {
        assertEquals(true,
            PasswordUtils.validatePassword(s, PasswordUtils.getPasswordHash(s)));
      } catch (PasswordHashFailureException e) {
        fail("Unexpected execption: " + e.getMessage());
      }
    }
  }

  @Test
  public void doesHashingAndCheckingFailCorrectly() {
    for (int i = 0; i < TEST_DATA.size(); i++) {
      try {
        assertEquals(false,
            PasswordUtils.validatePassword(TEST_DATA.get(i), PasswordUtils
                .getPasswordHash((i == TEST_DATA.size() - 1) ? TEST_FAIL_STRING
                    : TEST_DATA.get(i + 1))));
      } catch (PasswordHashFailureException e) {
        fail("Unexpected execption: " + e.getMessage());
      }
    }
  }
}
