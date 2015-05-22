package servlet;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Handles internationalisation of server messages based on user locale.
 */
public class Messages {

  private static final String BUNDLE_NAME = "servlet.messages"; //$NON-NLS-1$

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
      .getBundle(BUNDLE_NAME);

  private Messages() {}

  public static String getString(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
}
