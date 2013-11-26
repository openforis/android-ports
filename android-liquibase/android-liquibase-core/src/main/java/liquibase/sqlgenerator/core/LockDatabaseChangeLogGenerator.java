package liquibase.sqlgenerator.core;

import java.sql.Timestamp;

import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverter;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;

public class LockDatabaseChangeLogGenerator extends AbstractSqlGenerator<LockDatabaseChangeLogStatement> {

    public ValidationErrors validate(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    public Sql[] generateSql(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	String liquibaseSchema = database.getLiquibaseSchemaName();

        UpdateStatement updateStatement = new UpdateStatement(liquibaseSchema, database.getDatabaseChangeLogLockTableName());
        updateStatement.addNewColumnValue("LOCKED", true);
        updateStatement.addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()));
        updateStatement.addNewColumnValue("LOCKEDBY", "localhost");
        
        TypeConverter typeConverter = TypeConverterFactory.getInstance().findTypeConverter(database);
        String idColumnName = database.escapeColumnName(liquibaseSchema, database.getDatabaseChangeLogTableName(), "ID");
		String lockedColumnName = database.escapeColumnName(liquibaseSchema, database.getDatabaseChangeLogTableName(), "LOCKED");
		String falseBooleanValue = typeConverter.getBooleanType().getFalseBooleanValue();
		
		updateStatement.setWhereClause(idColumnName + " = 1 AND " + lockedColumnName + " = "+ falseBooleanValue);

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);
    }
}