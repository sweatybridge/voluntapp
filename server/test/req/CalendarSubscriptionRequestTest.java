package req;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CalendarSubscriptionRequestTest {

  private static final String VALID_JC_1 = "hello4";
  private static final String VALID_JC_2 = "4HELLO";

  private static final String INVALID_JC_1 = "HELLO";
  private static final String INVALID_JC_2 = "$HELLO";

  private CalendarSubscriptionRequest request;

  @Before
  public void setUp() {
    request = new CalendarSubscriptionRequest();
  }

  @Test
  public void testIsValidSucceedsWhenJoinCodeHas6AlphanumericCharacters() {
    request.setJoinCode(VALID_JC_1);
    assertTrue(request.isValid());
  }

  @Test
  public void testValidationIsCaseInsensitive() {
    request.setJoinCode(VALID_JC_2);
    assertTrue(request.isValid());
  }

  @Test
  public void testIsValidFailsWhenJoinCodeIsMalformed() {
    assertFalse(request.isValid());
    request.setJoinCode(INVALID_JC_1);
    assertFalse(request.isValid());
    request.setJoinCode(INVALID_JC_2);
    assertFalse(request.isValid());
  }
}
