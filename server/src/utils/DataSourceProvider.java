package utils;

import org.postgresql.ds.PGConnectionPoolDataSource;

/**
 * Wraps around the PGConnectionPoolDataSource to return only one instance of
 * it. Singleton.
 * 
 * @author nc1813
 * 
 */
public class DataSourceProvider {
  private static PGConnectionPoolDataSource source = null;

  /**
   * Creates the data source if not already created, otherwise returns the same
   * one.
   * 
   * @return data source
   */
  public static synchronized PGConnectionPoolDataSource getSource() {
    if (source == null) {
      source = new PGConnectionPoolDataSource();
      source.setUrl(ConfigResources.getString("dbUrl")); //$NON-NLS-1$
      source.setUser(ConfigResources.getString("dbUser")); //$NON-NLS-1$
      source.setPassword(ConfigResources.getString("dbPass")); //$NON-NLS-1$
      source.setSsl(true);
      source.setSslfactory("org.postgresql.ssl.NonValidatingFactory"); //$NON-NLS-1$
    }
    return source;
  }
}
