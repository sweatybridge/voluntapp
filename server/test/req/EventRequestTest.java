package req;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

public class EventRequestTest {

  private static final String TEST_TITLE = "My Event";
  private static final String TEST_DESCRIPTION = "My description.";
  private static final String TEST_LOCATION = "London";
  private static final String TEST_START_DATE = "2015-06-30";
  private static final String TEST_START_TIME = "14:45";
  private static final String TEST_END_DATE = "16-6-05";
  private static final String TEST_END_TIME = "8:5";
  private static final String TEST_TIMEZONE = "Europe/London";
  private static final int TEST_CAL_ID = 7;
  private static final int TEST_MAX = 50;

  private static final String TEST_BAD_DATE = "2015-2-29";
  private static final String TEST_BAD_DATE_2 = "12-2-3";
  private static final String TEST_BAD_TIME = "7:60";
  private static final String TEST_BAD_TIME_2 = "25:10";

  private final Gson gson = new Gson();

  @Test
  public void validationSucceedsWhenLocationDescriptionEndDateTimeAreMissing() {
    EventRequest event =
        new EventRequest(TEST_TITLE, null, null, TEST_START_TIME,
            TEST_START_DATE, null, null, TEST_TIMEZONE, TEST_MAX, TEST_CAL_ID);
    assertTrue(event.isValid());
  }

  @Test
  public void validationSucceedsWhenDateTimePatternIsGood() {
    EventRequest event =
        new EventRequest(TEST_TITLE, TEST_DESCRIPTION, TEST_LOCATION,
            TEST_START_TIME, TEST_START_DATE, TEST_END_TIME, TEST_END_DATE,
            TEST_TIMEZONE, TEST_MAX, TEST_CAL_ID);
    assertTrue(event.isValid());
  }

  @Test
  public void validationSucceedsWhenCalendarIdIsString() {
    String payload =
        gson.toJson(ImmutableMap.of("title", TEST_TITLE, "startDate",
            TEST_START_DATE, "startTime", TEST_START_TIME, "calendarId",
            Integer.toString(TEST_CAL_ID), "timezone", TEST_TIMEZONE));

    EventRequest event = gson.fromJson(payload, EventRequest.class);

    assertTrue(event.isValid());
  }

  @Test
  public void validationFailsWhenTimezoneIsInvalid() {
    EventRequest event =
        new EventRequest(TEST_TITLE, null, null, TEST_BAD_TIME,
            TEST_START_DATE, null, null, "Moon", TEST_MAX, TEST_CAL_ID);

    assertFalse(event.isValid());
  }

  @Test
  public void validationFailsWhenTimePatternIsBad() {
    EventRequest event =
        new EventRequest(TEST_TITLE, null, null, TEST_BAD_TIME,
            TEST_START_DATE, null, null, TEST_TIMEZONE, TEST_MAX, TEST_CAL_ID);

    EventRequest event2 =
        new EventRequest(TEST_TITLE, null, null, TEST_BAD_TIME_2,
            TEST_START_DATE, null, null, TEST_TIMEZONE, TEST_MAX, TEST_CAL_ID);

    assertFalse(event.isValid());
    assertFalse(event2.isValid());
  }

  @Test
  public void validationFailsWhenDatePatternIsBad() {
    EventRequest event =
        new EventRequest(TEST_TITLE, null, null, TEST_START_TIME,
            TEST_BAD_DATE, null, null, TEST_TIMEZONE, TEST_MAX, TEST_CAL_ID);

    EventRequest event2 =
        new EventRequest(TEST_TITLE, null, null, TEST_START_TIME,
            TEST_BAD_DATE_2, null, null, TEST_TIMEZONE, TEST_MAX, TEST_CAL_ID);

    assertFalse(event.isValid());
    assertFalse(event2.isValid());
  }

  @Test
  public void validationFailsWhenTitleIsEmpty() {
    EventRequest event =
        new EventRequest("", null, null, TEST_START_TIME, TEST_START_DATE,
            null, null, TEST_TIMEZONE, TEST_MAX, TEST_CAL_ID);

    assertFalse(event.isValid());
  }

  @Test
  public void validationFailsWhenStartTimeIsEmpty() {
    EventRequest event =
        new EventRequest(TEST_TITLE, null, null, TEST_START_DATE, "", null,
            null, TEST_TIMEZONE, TEST_MAX, TEST_CAL_ID);

    assertFalse(event.isValid());
  }

  @Test
  public void validationFailsWhenMaxIsNegative() {
    EventRequest event =
        new EventRequest(TEST_TITLE, null, null, TEST_START_TIME,
            TEST_START_DATE, null, null, TEST_TIMEZONE, -1, TEST_CAL_ID);

    assertFalse(event.isValid());
  }

  @Test
  public void validationFailsWhenCalendarIdIsNotSetOrZero() {
    String payload =
        gson.toJson(ImmutableMap.of("title", TEST_TITLE, "startDate",
            TEST_START_DATE, "startTime", TEST_START_TIME, "timezone",
            TEST_TIMEZONE));

    EventRequest event = gson.fromJson(payload, EventRequest.class);

    assertFalse(event.isValid());
  }
}
