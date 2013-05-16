forked from git@github.com:liquibase/liquibase.git

Classes modified to support Android:
pom.xml (disabled tests, changed groupId/artifactId)
liquibase-core/src/main/java/liquibase/servicelocator/ServiceLocator.java (class loader not working properly on Android)
liquibase-core/src/main/java/liquibase/database/AbstractDatabase.java (support "SQLite for Android" database product number)

Added:
liquibase-core/src/main/java/liquibase/database/core/AndroidSQLiteDatabase.java