package utils;

import org.postgresql.ds.PGConnectionPoolDataSource;

public class DataSourceProvider {
  private static PGConnectionPoolDataSource source = null;

  public static synchronized PGConnectionPoolDataSource getSource() {
    if (source == null) {
      source = new PGConnectionPoolDataSource();
      source.setUrl(DBParameters.getString("dbUrl")); //$NON-NLS-1$
      source.setUser(DBParameters.getString("dbUser")); //$NON-NLS-1$
      source.setPassword(DBParameters.getString("dbPass")); //$NON-NLS-1$
      source.setSsl(true);
      source.setSslfactory("org.postgresql.ssl.NonValidatingFactory"); //$NON-NLS-1$
    }
    return source;
  }
}
