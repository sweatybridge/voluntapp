package req;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.gson.Gson;

public class RegisterRequestTest {

  private Gson gson = new Gson();

  @Test
  public void validationSucceedsWithConfPasswordField() {
    String json =
        "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\",\"conf_password\":\"123123\",\"firstName\":\"han\",\"lastName\":\"qiao\"}";

    RegisterRequest user = gson.fromJson(json, RegisterRequest.class);

    assertTrue(user.isValid());
  }

  @Test
  public void validationSucceedsWithEmptyLastName() {
    String json =
        "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\",\"conf_password\":\"123123\",\"firstName\":\"han\",\"lastName\":\"\"}";

    RegisterRequest user = gson.fromJson(json, RegisterRequest.class);

    assertTrue(user.isValid());
  }

  @Test
  public void validationSucceedsWithNullLastName() {
    String json =
        "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\",\"conf_password\":\"123123\",\"firstName\":\"han\",\"lastName\":\"\"}";

    RegisterRequest user = gson.fromJson(json, RegisterRequest.class);

    assertTrue(user.isValid());
  }

  @Test
  public void validationSucceedsWithWellFormedData() {
    String json =
        "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\",\"conf_password\":\"123123\",\"firstName\":\"han\",\"lastName\":\"\"}";

    RegisterRequest user = gson.fromJson(json, RegisterRequest.class);

    assertTrue(user.isValid());
  }

  @Test
  public void validationFailsWithInvalidEmail() {
    String json =
        "{\"email\":\"abc123\",\"password\":\"123123\",\"conf_password\":\"123123\",\"firstName\":\"han\",\"lastName\":\"\"}";

    RegisterRequest user = gson.fromJson(json, RegisterRequest.class);

    assertFalse(user.isValid());
  }

  @Test
  public void validationFailsWithShortPassword() {
    String json =
        "{\"email\":\"abc123@gmail.com\",\"password\":\"12312\",\"conf_password\":\"12312\",\"firstName\":\"han\",\"lastName\":\"\"}";

    RegisterRequest user = gson.fromJson(json, RegisterRequest.class);

    assertFalse(user.isValid());
  }

  @Test
  public void validationFailsWithEmptyFirstName() {
    String json =
        "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\",\"conf_password\":\"123123\",\"firstName\":\"\",\"lastName\":\"\"}";

    RegisterRequest user = gson.fromJson(json, RegisterRequest.class);

    assertFalse(user.isValid());
  }

  @Test
  public void validationFailsWithNullFirstName() {
    String json =
        "{\"email\":\"abc123@gmail.com\",\"password\":\"123123\",\"conf_password\":\"123123\"}";

    RegisterRequest user = gson.fromJson(json, RegisterRequest.class);

    assertFalse(user.isValid());
  }
}
