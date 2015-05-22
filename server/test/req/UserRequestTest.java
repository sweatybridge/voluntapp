package req;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.gson.Gson;

public class UserRequestTest {

  private final Gson gson = new Gson();

  @Test
  public void validationSucceedsWithExtraFields() {
    String json =
        "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\",\"extra\":\"field\"}";
    UserRequest user = gson.fromJson(json, UserRequest.class);
    assertTrue(user.isValid());
  }

  @Test
  public void validationFailsWhenPasswordIsNull() {
    String json = "{\"email\":\"abc123@gmail.com\"}";
    UserRequest user = gson.fromJson(json, UserRequest.class);
    assertFalse(user.isValid());
  }

  @Test
  public void validationFailsWhenPasswordIsShorterThan6() {
    String json = "{\"email\":\"abc123@gmail.com\",\"password\":\"12312\"}";
    UserRequest user = gson.fromJson(json, UserRequest.class);
    assertFalse(user.isValid());
  }

  @Test
  public void validationFailsWhenEmailIsMalformed() {
    String json = "{\"email\":\"abc123\",\"password\":\"123123\"}";
    UserRequest user = gson.fromJson(json, UserRequest.class);
    assertFalse(user.isValid());
  }
}
