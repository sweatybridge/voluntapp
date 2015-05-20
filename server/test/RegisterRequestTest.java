import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class RegisterRequestTest {

  @Test
  public void testIsValid() {
    RegisterRequest user = new RegisterRequest();
    assertFalse(user.isValid());
  }

}
