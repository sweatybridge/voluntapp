package req;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.gson.Gson;

public class LoginRequestTest {

  private final Gson gson = new Gson();

  @Test
  public void validationSucceedsWithExtraFields() {
    String json =
        "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\",\"extra\":\"field\"}";
    LoginRequest user = gson.fromJson(json, LoginRequest.class);
    assertTrue(user.isValid());
  }

  @Test
  public void validationFailsWhenPasswordIsNull() {
    String json = "{\"email\":\"abc123@gmail.com\"}";
    LoginRequest user = gson.fromJson(json, LoginRequest.class);
    assertFalse(user.isValid());
  }

  @Test
  public void validationFailsWhenPasswordIsShorterThan6() {
    String json = "{\"email\":\"abc123@gmail.com\",\"password\":\"12312\"}";
    LoginRequest user = gson.fromJson(json, LoginRequest.class);
    assertFalse(user.isValid());
  }

  @Test
  public void validationFailsWhenEmailIsMalformed() {
    String json = "{\"email\":\"abc123\",\"password\":\"123123\"}";
    LoginRequest user = gson.fromJson(json, LoginRequest.class);
    assertFalse(user.isValid());
  }
}
