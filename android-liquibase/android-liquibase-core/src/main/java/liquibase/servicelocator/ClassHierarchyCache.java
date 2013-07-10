package liquibase.servicelocator;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author S. Ricci
 *
 */
@SuppressWarnings("rawtypes")
public abstract class ClassHierarchyCache {

	private static Map<Class, Type[]> genericInterfacesByClass;
	private static Map<Class, Type> genericSuperclassByClass;
	private static Map<Class, Class> superclassByClass;
	
	public static Type[] getGenericInterfaces(Class clazz) {
		if ( genericInterfacesByClass == null ) {
			genericInterfacesByClass = new HashMap<Class, Type[]>();
		}
		Type[] result = genericInterfacesByClass.get(clazz);
		if ( result == null ) {
			result = clazz.getGenericInterfaces();
			genericInterfacesByClass.put(clazz, result);
		}
		return result;
	}

	public static Type getGenericSuperclass(Class clazz) {
		if ( genericSuperclassByClass == null ) {
			genericSuperclassByClass = new HashMap<Class, Type>();
		}
		Type result = genericSuperclassByClass.get(clazz);
		if ( result == null ) {
			result = clazz.getGenericSuperclass();
			genericSuperclassByClass.put(clazz, result);
		}
		return result;
	}

	public static Class getSuperclass(Class clazz) {
		if ( superclassByClass == null ) {
			superclassByClass = new HashMap<Class, Class>();
		}
		Class result = superclassByClass.get(clazz);
		if ( result == null ) {
			result = clazz.getSuperclass();
			superclassByClass.put(clazz, result);
		}
		return result;
	}
	
}
