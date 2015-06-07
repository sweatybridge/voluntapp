package utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class DBParameters {
  private static final String BUNDLE_NAME = "resource.db"; //$NON-NLS-1$

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
      .getBundle(BUNDLE_NAME);

  private DBParameters() {
  }

  public static String getString(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
}
