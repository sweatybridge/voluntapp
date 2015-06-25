package utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ConfigResources {
  private static final String BUNDLE_NAME = "resource.config"; //$NON-NLS-1$

  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
      .getBundle(BUNDLE_NAME);

  private ConfigResources() {
  }

  public static String getString(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
}
