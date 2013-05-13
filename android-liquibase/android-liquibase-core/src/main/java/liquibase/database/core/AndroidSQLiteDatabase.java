package liquibase.database.core;

import liquibase.database.DatabaseConnection;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.DatabaseException;

/**
 * 
 * @author S. Ricci
 *
 */
public class AndroidSQLiteDatabase extends SQLiteDatabase {

	public static final String PRODUCT_NAME = "SQLite for Android";

	@Override
	public boolean isLocalDatabase() throws DatabaseException {
		return true;
	}
	
	 public boolean isCorrectDatabaseImplementation(DatabaseConnection conn)
	            throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }
	
}
