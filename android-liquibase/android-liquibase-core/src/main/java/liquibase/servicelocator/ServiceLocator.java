package liquibase.servicelocator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

import liquibase.change.Change;
import liquibase.change.core.AddAutoIncrementChange;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddDefaultValueChange;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.AddLookupTableChange;
import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.core.AlterSequenceChange;
import liquibase.change.core.AnonymousChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.change.core.CreateProcedureChange;
import liquibase.change.core.CreateSequenceChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.CreateViewChange;
import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.DropAllForeignKeyConstraintsChange;
import liquibase.change.core.DropColumnChange;
import liquibase.change.core.DropDefaultValueChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.change.core.DropIndexChange;
import liquibase.change.core.DropNotNullConstraintChange;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.change.core.DropSequenceChange;
import liquibase.change.core.DropTableChange;
import liquibase.change.core.DropUniqueConstraintChange;
import liquibase.change.core.DropViewChange;
import liquibase.change.core.EmptyChange;
import liquibase.change.core.ExecuteShellCommandChange;
import liquibase.change.core.InsertDataChange;
import liquibase.change.core.LoadDataChange;
import liquibase.change.core.LoadUpdateDataChange;
import liquibase.change.core.MergeColumnChange;
import liquibase.change.core.ModifyDataTypeChange;
import liquibase.change.core.RawSQLChange;
import liquibase.change.core.RenameColumnChange;
import liquibase.change.core.RenameTableChange;
import liquibase.change.core.RenameViewChange;
import liquibase.change.core.SQLFileChange;
import liquibase.change.core.StopChange;
import liquibase.change.core.TagDatabaseChange;
import liquibase.change.core.UpdateDataChange;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.typeconversion.TypeConverter;
import liquibase.database.typeconversion.core.SQLiteTypeConverter;
import liquibase.exception.ServiceNotFoundException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.logging.Logger;
import liquibase.logging.core.DefaultLogger;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.core.formattedsql.FormattedSqlChangeLogParser;
import liquibase.parser.core.sql.SqlChangeLogParser;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.precondition.CustomPreconditionWrapper;
import liquibase.precondition.Precondition;
import liquibase.precondition.core.AndPrecondition;
import liquibase.precondition.core.ChangeLogPropertyDefinedPrecondition;
import liquibase.precondition.core.ChangeSetExecutedPrecondition;
import liquibase.precondition.core.ColumnExistsPrecondition;
import liquibase.precondition.core.DBMSPrecondition;
import liquibase.precondition.core.ForeignKeyExistsPrecondition;
import liquibase.precondition.core.IndexExistsPrecondition;
import liquibase.precondition.core.NotPrecondition;
import liquibase.precondition.core.OrPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.PrimaryKeyExistsPrecondition;
import liquibase.precondition.core.RunningAsPrecondition;
import liquibase.precondition.core.SequenceExistsPrecondition;
import liquibase.precondition.core.SqlPrecondition;
import liquibase.precondition.core.TableExistsPrecondition;
import liquibase.precondition.core.ViewExistsPrecondition;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.snapshot.jvm.SQLiteDatabaseSnapshotGenerator;
import liquibase.snapshot.jvm.StandardJdbcDatabaseSnapshotGenerator;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.core.AddAutoIncrementGenerator;
import liquibase.sqlgenerator.core.AddAutoIncrementGeneratorSQLite;
import liquibase.sqlgenerator.core.AddColumnGenerator;
import liquibase.sqlgenerator.core.AddColumnGeneratorDefaultClauseBeforeNotNull;
import liquibase.sqlgenerator.core.AddColumnGeneratorSQLite;
import liquibase.sqlgenerator.core.AddDefaultValueGenerator;
import liquibase.sqlgenerator.core.AddDefaultValueSQLite;
import liquibase.sqlgenerator.core.AddForeignKeyConstraintGenerator;
import liquibase.sqlgenerator.core.AddPrimaryKeyGenerator;
import liquibase.sqlgenerator.core.AddUniqueConstraintGenerator;
import liquibase.sqlgenerator.core.AlterSequenceGenerator;
import liquibase.sqlgenerator.core.ClearDatabaseChangeLogTableGenerator;
import liquibase.sqlgenerator.core.CommentGenerator;
import liquibase.sqlgenerator.core.CopyRowsGenerator;
import liquibase.sqlgenerator.core.CreateDatabaseChangeLogLockTableGenerator;
import liquibase.sqlgenerator.core.CreateDatabaseChangeLogTableGenerator;
import liquibase.sqlgenerator.core.CreateIndexGenerator;
import liquibase.sqlgenerator.core.CreateSequenceGenerator;
import liquibase.sqlgenerator.core.CreateTableGenerator;
import liquibase.sqlgenerator.core.CreateViewGenerator;
import liquibase.sqlgenerator.core.DeleteGenerator;
import liquibase.sqlgenerator.core.DropColumnGenerator;
import liquibase.sqlgenerator.core.DropDefaultValueGenerator;
import liquibase.sqlgenerator.core.DropForeignKeyConstraintGenerator;
import liquibase.sqlgenerator.core.DropIndexGenerator;
import liquibase.sqlgenerator.core.DropPrimaryKeyGenerator;
import liquibase.sqlgenerator.core.DropSequenceGenerator;
import liquibase.sqlgenerator.core.DropTableGenerator;
import liquibase.sqlgenerator.core.DropUniqueConstraintGenerator;
import liquibase.sqlgenerator.core.DropViewGenerator;
import liquibase.sqlgenerator.core.GetNextChangeSetSequenceValueGenerator;
import liquibase.sqlgenerator.core.GetViewDefinitionGenerator;
import liquibase.sqlgenerator.core.InsertGenerator;
import liquibase.sqlgenerator.core.LockDatabaseChangeLogGenerator;
import liquibase.sqlgenerator.core.MarkChangeSetRanGenerator;
import liquibase.sqlgenerator.core.ModifyDataTypeGenerator;
import liquibase.sqlgenerator.core.RawSqlGenerator;
import liquibase.sqlgenerator.core.ReindexGeneratorSQLite;
import liquibase.sqlgenerator.core.RemoveChangeSetRanStatusGenerator;
import liquibase.sqlgenerator.core.RenameColumnGenerator;
import liquibase.sqlgenerator.core.RenameTableGenerator;
import liquibase.sqlgenerator.core.RenameViewGenerator;
import liquibase.sqlgenerator.core.RuntimeGenerator;
import liquibase.sqlgenerator.core.SelectFromDatabaseChangeLogGenerator;
import liquibase.sqlgenerator.core.SelectFromDatabaseChangeLogLockGenerator;
import liquibase.sqlgenerator.core.SetColumnRemarksGenerator;
import liquibase.sqlgenerator.core.SetNullableGenerator;
import liquibase.sqlgenerator.core.SetTableRemarksGenerator;
import liquibase.sqlgenerator.core.StoredProcedureGenerator;
import liquibase.sqlgenerator.core.TagDatabaseGenerator;
import liquibase.sqlgenerator.core.UnlockDatabaseChangeLogGenerator;
import liquibase.sqlgenerator.core.UpdateChangeSetChecksumGenerator;
import liquibase.sqlgenerator.core.UpdateGenerator;
import liquibase.util.StringUtils;

public class ServiceLocator {

    private static ServiceLocator instance;

    static {
        try {
            Class<?> scanner = Class.forName("Liquibase.ServiceLocator.ClrServiceLocator, Liquibase");
            instance = (ServiceLocator) scanner.newInstance();
        } catch (Exception e) {
            instance = new ServiceLocator();
        }
    }

    private ResourceAccessor resourceAccessor;

    private Map<Class, List<Class>> classesBySuperclass;
    private List<String> packagesToScan;
    private Logger logger = new DefaultLogger(); //cannot look up regular logger because you get a stackoverflow since we are in the servicelocator
    private PackageScanClassResolver classResolver;

    protected ServiceLocator() {
        this.classResolver = defaultClassLoader();
        setResourceAccessor(new ClassLoaderResourceAccessor());
    }

    protected ServiceLocator(ResourceAccessor accessor) {
        this.classResolver = defaultClassLoader();
        setResourceAccessor(accessor);
    }

    protected ServiceLocator(PackageScanClassResolver classResolver) {
        this.classResolver = classResolver;
        setResourceAccessor(new ClassLoaderResourceAccessor());
    }

    protected ServiceLocator(PackageScanClassResolver classResolver, ResourceAccessor accessor) {
        this.classResolver = classResolver;
        setResourceAccessor(accessor);
    }

    public static ServiceLocator getInstance() {
        return instance;
    }

    public static void setInstance(ServiceLocator newInstance) {
        instance = newInstance;
    }

    private PackageScanClassResolver defaultClassLoader(){
        if (WebSpherePackageScanClassResolver.isWebSphereClassLoader(this.getClass().getClassLoader())) {
            logger.debug("Using WebSphere Specific Class Resolver");
            return new WebSpherePackageScanClassResolver("liquibase/parser/core/xml/dbchangelog-2.0.xsd");
        } else {
            return new DefaultPackageScanClassResolver();
        }
    }

    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
        this.classesBySuperclass = new HashMap<Class, List<Class>>();

        this.classResolver.setClassLoaders(new HashSet<ClassLoader>(Arrays.asList(new ClassLoader[] {resourceAccessor.toClassLoader()})));

        packagesToScan = new ArrayList<String>();
        String packagesToScanSystemProp = System.getProperty("liquibase.scan.packages");
        if ((packagesToScanSystemProp != null) &&
        	((packagesToScanSystemProp = StringUtils.trimToNull(packagesToScanSystemProp)) != null)) {
        	for (String value : packagesToScanSystemProp.split(",")) {
        		addPackageToScan(value);
        	}
        } else {
	        Enumeration<URL> manifests = null;
	        try {
	            manifests = resourceAccessor.getResources("META-INF/MANIFEST.MF");
	            while (manifests.hasMoreElements()) {
	                URL url = manifests.nextElement();
	                InputStream is = url.openStream();
	                Manifest manifest = new Manifest(is);
	                String attributes = StringUtils.trimToNull(manifest.getMainAttributes().getValue("Liquibase-Package"));
	                if (attributes != null) {
	                    for (Object value : attributes.split(",")) {
	                        addPackageToScan(value.toString());
	                    }
	                }
	                is.close();
	            }
	        } catch (IOException e) {
	            throw new UnexpectedLiquibaseException(e);
	        }

            if (packagesToScan.size() == 0) {
                addPackageToScan("liquibase.change");
                addPackageToScan("liquibase.database");
                addPackageToScan("liquibase.parser");
                addPackageToScan("liquibase.precondition");
                addPackageToScan("liquibase.serializer");
                addPackageToScan("liquibase.sqlgenerator");
                addPackageToScan("liquibase.executor");
                addPackageToScan("liquibase.snapshot");
                addPackageToScan("liquibase.logging");
                addPackageToScan("liquibase.ext");
            }
        }
    }

    public void addPackageToScan(String packageName) {
        packagesToScan.add(packageName);
    }

    public Class findClass(Class requiredInterface) throws ServiceNotFoundException {
    	if  (requiredInterface == Logger.class ) {
    		return DefaultLogger.class;
    	} else if ( requiredInterface == Executor.class ) {
    		return JdbcExecutor.class;
    	} else {
            throw new ServiceNotFoundException("Could not find implementation of " + requiredInterface.getName());
    	}
     	/*
        Class[] classes = findClasses(requiredInterface);
        if (PrioritizedService.class.isAssignableFrom(requiredInterface)) {
            PrioritizedService returnObject = null;
            for (Class clazz : classes) {
                PrioritizedService newInstance;
                try {
                    newInstance = (PrioritizedService) clazz.newInstance();
                } catch (Exception e) {
                    throw new UnexpectedLiquibaseException(e);
                }

                if (returnObject == null || newInstance.getPriority() > returnObject.getPriority()) {
                    returnObject = newInstance;
                }
            }

            if (returnObject == null) {
                throw new ServiceNotFoundException("Could not find implementation of " + requiredInterface.getName());
            }
            return returnObject.getClass();
        }

        if (classes.length != 1) {
            throw new ServiceNotFoundException("Could not find unique implementation of " + requiredInterface.getName() + ".  Found " + classes.length + " implementations");
        }

        return classes[0];
        */
    }

    public Class[] findClasses(Class requiredInterface) throws ServiceNotFoundException {
        logger.debug("ServiceLocator.findClasses for "+requiredInterface.getName());

            try {
                Class.forName(requiredInterface.getName());

                if (!classesBySuperclass.containsKey(requiredInterface)) {
                    classesBySuperclass.put(requiredInterface, findClassesImpl(requiredInterface));
                }
            } catch (Exception e) {
                throw new ServiceNotFoundException(e);
            }

        List<Class> classes = classesBySuperclass.get(requiredInterface);
        HashSet<Class> uniqueClasses = new HashSet<Class>(classes);
        return uniqueClasses.toArray(new Class[uniqueClasses.size()]);
    }

    public Object newInstance(Class requiredInterface) throws ServiceNotFoundException {
        try {
            return findClass(requiredInterface).newInstance();
        } catch (Exception e) {
            throw new ServiceNotFoundException(e);
        }
    }

    private List<Class> findClassesImpl(Class requiredInterface) throws Exception {
        logger.debug("ServiceLocator finding classes matching interface " + requiredInterface.getName());
        /*
        List<Class> classes = new ArrayList<Class>();

        classResolver.addClassLoader(resourceAccessor.toClassLoader());
        for (Class<?> clazz : classResolver.findImplementations(requiredInterface, packagesToScan.toArray(new String[packagesToScan.size()]))) {
            if (clazz.getAnnotation(LiquibaseService.class ) != null  && clazz.getAnnotation(LiquibaseService.class).skip()) {
                continue;
            }

            if (!Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers()) && Modifier.isPublic(clazz.getModifiers())) {
                try {
                    clazz.getConstructor();
                    logger.debug(clazz.getName() + " matches "+requiredInterface.getName());

                    classes.add(clazz);
                } catch (NoSuchMethodException e) {
                    logger.info("Can not use "+clazz+" as a Liquibase service because it does not have a no-argument constructor" );
                }
            }
        }
        return classes;
         */
    	return Arrays.asList(getImpl(requiredInterface));

    }

	private Class[] getImpl(Class requiredInterface) {
		if ( requiredInterface == Logger.class ) {
    		return new Class[]{DefaultLogger.class};
    	} else if ( requiredInterface == Change.class ) {
    		return new Class[]{
    				RawSQLChange.class, 
    				SQLFileChange.class,
    				AddAutoIncrementChange.class, 
    				AddColumnChange.class, 
    				AddDefaultValueChange.class, 
    				AddForeignKeyConstraintChange.class, 
    				AddLookupTableChange.class, 
    				AddNotNullConstraintChange.class, 
    				AddPrimaryKeyChange.class, 
    				AddUniqueConstraintChange.class, 
    				AlterSequenceChange.class, 
    				AnonymousChange.class, 
    				CreateIndexChange.class, 
    				CreateProcedureChange.class, 
    				CreateSequenceChange.class, 
    				CreateTableChange.class, 
    				CreateViewChange.class, 
    				CustomChangeWrapper.class, 
    				DeleteDataChange.class, 
    				DropAllForeignKeyConstraintsChange.class, 
    				DropColumnChange.class, 
    				DropDefaultValueChange.class, 
    				DropForeignKeyConstraintChange.class, 
    				DropIndexChange.class, 
    				DropNotNullConstraintChange.class, 
    				DropPrimaryKeyChange.class, 
    				DropSequenceChange.class, 
    				DropTableChange.class, 
    				DropUniqueConstraintChange.class, 
    				DropViewChange.class, 
    				EmptyChange.class, 
    				ExecuteShellCommandChange.class, 
    				InsertDataChange.class, 
    				LoadDataChange.class, 
    				LoadUpdateDataChange.class, 
    				MergeColumnChange.class, 
    				ModifyDataTypeChange.class, 
    				RenameColumnChange.class, 
    				RenameTableChange.class, 
    				RenameViewChange.class, 
    				StopChange.class, 
    				TagDatabaseChange.class, 
    				UpdateDataChange.class 
    				};
    	} else if ( requiredInterface == TypeConverter.class ) {
    		return new Class[]{SQLiteTypeConverter.class};
    	} else if ( requiredInterface == ChangeLogParser.class ) {
    		return new Class[]{
    				FormattedSqlChangeLogParser.class,
    				SqlChangeLogParser.class,
    				XMLChangeLogSAXParser.class
    				};
    	} else if ( requiredInterface == Precondition.class ) {
    		return new Class[]{
    				ChangeLogPropertyDefinedPrecondition.class,
    				ChangeSetExecutedPrecondition.class,
    				ColumnExistsPrecondition.class,
    				CustomPreconditionWrapper.class,
    				DBMSPrecondition.class,
    				ForeignKeyExistsPrecondition.class,
    				IndexExistsPrecondition.class,
    				//MockPrecondition.class
    				AndPrecondition.class,
    				PreconditionContainer.class,
    				NotPrecondition.class,
    				OrPrecondition.class,
    				PrimaryKeyExistsPrecondition.class,
    				RunningAsPrecondition.class,
    				SequenceExistsPrecondition.class,
    				SqlPrecondition.class,
    				TableExistsPrecondition.class,
    				ViewExistsPrecondition.class
    			};
		} else if ( requiredInterface == ChangeLogSerializer.class ) {
    		return new Class[]{
    				//MockChangeLogSerializer.class
    				StringChangeLogSerializer.class,
    				XMLChangeLogSerializer.class
    		};
		} else if ( requiredInterface == Database.class ) {
    		return new Class[]{
    				SQLiteDatabase.class
    		};
		} else if ( requiredInterface == DatabaseSnapshotGenerator.class ) {
    		return new Class[]{
    				StandardJdbcDatabaseSnapshotGenerator.class,
    				SQLiteDatabaseSnapshotGenerator.class
    		};
		} else if ( requiredInterface == SqlGenerator.class ) {
			return new Class[] {
					AddAutoIncrementGenerator.class,
//					AddAutoIncrementGeneratorDB2.class,
//					AddAutoIncrementGeneratorHsqlH2.class,
//					AddAutoIncrementGeneratorInformix.class,
					AddAutoIncrementGeneratorSQLite.class,
					AddColumnGenerator.class,
					AddColumnGeneratorDefaultClauseBeforeNotNull.class,
					AddColumnGeneratorSQLite.class,
					AddDefaultValueGenerator.class,
//					AddDefaultValueGeneratorDerby.class,
//					AddDefaultValueGeneratorInformix.class,
//					AddDefaultValueGeneratorMaxDB.class,
//					AddDefaultValueGeneratorMSSQL.class,
//					AddDefaultValueGeneratorMySQL.class,
//					AddDefaultValueGeneratorOracle.class,
//					AddDefaultValueGeneratorSybase.class,
//					AddDefaultValueGeneratorSybaseASA.class,
					AddDefaultValueSQLite.class,
					AddForeignKeyConstraintGenerator.class,
					AddPrimaryKeyGenerator.class,
//					AddPrimaryKeyGeneratorInformix.class,
					AddUniqueConstraintGenerator.class,
//					AddUniqueConstraintGeneratorInformix.class,
//					AddUniqueConstraintGeneratorTDS.class,
					AlterSequenceGenerator.class,
					ClearDatabaseChangeLogTableGenerator.class,
					CommentGenerator.class,
					CopyRowsGenerator.class,
					CreateDatabaseChangeLogLockTableGenerator.class,
					CreateDatabaseChangeLogTableGenerator.class,
//					CreateDatabaseChangeLogTableGeneratorFirebird.class,
//					CreateDatabaseChangeLogTableGeneratorSybase.class,
					CreateIndexGenerator.class,
//					CreateIndexGeneratorPostgres.class,
					CreateSequenceGenerator.class,
					CreateTableGenerator.class,
					CreateViewGenerator.class,
					DeleteGenerator.class,
					DropColumnGenerator.class,
					DropDefaultValueGenerator.class,
					DropForeignKeyConstraintGenerator.class,
					DropIndexGenerator.class,
					DropPrimaryKeyGenerator.class,
					DropSequenceGenerator.class,
					DropTableGenerator.class,
					DropUniqueConstraintGenerator.class,
					DropViewGenerator.class,
//					FindForeignKeyConstraintsGeneratorDB2.class,
//					FindForeignKeyConstraintsGeneratorMSSQL.class,
//					FindForeignKeyConstraintsGeneratorMySQL.class,
//					FindForeignKeyConstraintsGeneratorOracle.class,
//					FindForeignKeyConstraintsGeneratorPostgres.class,
					GetNextChangeSetSequenceValueGenerator.class,
					GetViewDefinitionGenerator.class,
//					GetViewDefinitionGeneratorDB2.class,
//					GetViewDefinitionGeneratorDerby.class,
//					GetViewDefinitionGeneratorFirebird.class,
//					GetViewDefinitionGeneratorHsql.class,
//					GetViewDefinitionGeneratorInformationSchemaViews.class,
//					GetViewDefinitionGeneratorInformix.class,
//					GetViewDefinitionGeneratorMaxDB.class,
//					GetViewDefinitionGeneratorMSSQL.class,
//					GetViewDefinitionGeneratorOracle.class,
//					GetViewDefinitionGeneratorPostgres.class,
//					GetViewDefinitionGeneratorSybase.class,
//					GetViewDefinitionGeneratorSybaseASA.class,
					InsertGenerator.class,
//					InsertOrUpdateGenerator.class,
//					InsertOrUpdateGeneratorDB2.class,
//					InsertOrUpdateGeneratorH2.class,
//					InsertOrUpdateGeneratorHsql.class,
//					InsertOrUpdateGeneratorMSSQL.class,
//					InsertOrUpdateGeneratorMySQL.class,
//					InsertOrUpdateGeneratorOracle.class,
//					InsertOrUpdateGeneratorPostgres.class,
					LockDatabaseChangeLogGenerator.class,
					MarkChangeSetRanGenerator.class,
					ModifyDataTypeGenerator.class,
					RawSqlGenerator.class,
					ReindexGeneratorSQLite.class,
					RemoveChangeSetRanStatusGenerator.class,
					RenameColumnGenerator.class,
					RenameTableGenerator.class,
					RenameViewGenerator.class,
//					ReorganizeTableGeneratorDB2.class,
					RuntimeGenerator.class,
					SelectFromDatabaseChangeLogGenerator.class,
					SelectFromDatabaseChangeLogLockGenerator.class,
//					SelectSequencesGeneratorDB2.class,
//					SelectSequencesGeneratorDerby.class,
//					SelectSequencesGeneratorFirebird.class,
//					SelectSequencesGeneratorH2.class,
//					SelectSequencesGeneratorHsql.class,
//					SelectSequencesGeneratorInformix.class,
//					SelectSequencesGeneratorMaxDB.class,
//					SelectSequencesGeneratorOracle.class,
//					SelectSequencesGeneratorPostgres.class,
					SetColumnRemarksGenerator.class,
					SetNullableGenerator.class,
					SetTableRemarksGenerator.class,
					StoredProcedureGenerator.class,
					TagDatabaseGenerator.class,
					UnlockDatabaseChangeLogGenerator.class,
					UpdateChangeSetChecksumGenerator.class,
					UpdateGenerator.class
			};
    	} else {
    		throw new ServiceNotFoundException(requiredInterface.getName());
    	}
	}

    public static void reset() {
        instance = new ServiceLocator();
    }

    protected Logger getLogger() {
        return logger;
    }
}