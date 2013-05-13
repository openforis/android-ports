package liquibase.logging;

import java.util.HashMap;
import java.util.Map;

import liquibase.exception.ServiceNotFoundException;
import liquibase.servicelocator.ServiceLocator;

public class LogFactory {
    private static Map<String, Logger> loggers = new HashMap<String, Logger>();
    private static String defaultLoggingLevel = "info";

    public static Logger getLogger(String name) {
        if (!loggers.containsKey(name)) {
            Logger value;
            try {
                value = (Logger) ServiceLocator.getInstance().newInstance(Logger.class);
            } catch (Exception e) {
                throw new ServiceNotFoundException(e);
            }
            value.setName(name);
            value.setLogLevel(defaultLoggingLevel);
            loggers.put(name, value);
        }

        return loggers.get(name);
    }

    public static Logger getLogger() {
        return getLogger("liquibase");
    }

    public static void putLogger(Logger logger) {
    	putLogger("liquibase", logger);
    }
    
    public static void putLogger(String name, Logger logger) {
    	loggers.put(name, logger);
    }
    
    public static void setLoggingLevel(String defaultLoggingLevel) {
        LogFactory.defaultLoggingLevel = defaultLoggingLevel;
    }
}
