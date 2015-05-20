import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class LoginRequestTest {

  @Test
  public void testIsValid() {
    LoginRequest user = new LoginRequest();
    assertFalse(user.isValid());
  }

}
