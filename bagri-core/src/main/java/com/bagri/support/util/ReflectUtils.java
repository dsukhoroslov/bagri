package com.bagri.support.util;

import java.lang.reflect.Constructor;

/**
 * A set of static utility methods regarding reflection
 * 
 * @author Denis Sukhoroslov
 *
 */
public class ReflectUtils {

	/**
	 * Return Class for the class name provided. Return primitive class for primitive names
	 * 
	 * @param type the Java primitive or class name 
	 * @return the Class constructed from the type passed
	 * @throws ClassNotFoundException if no such Class found
	 */
	public static Class type2Class(String type) throws ClassNotFoundException {
		switch (type) {
			case "boolean": return boolean.class; 
			case "byte": return byte.class; 
			case "char": return char.class; 
			case "double": return double.class; 
			case "float": return float.class; 
			case "int": return int.class; 
			case "integer": return int.class; 
			case "long": return long.class; 
			case "short": return short.class; 
			case "string": return java.lang.String.class; 
		}
		return Class.forName(type);
	}
	
	/**
	 * Return Class for the class name provided. Return wrapper class for primitive names
	 * 
	 * @param type the Java primitive or class name
	 * @return the Class constructed from the type passed
	 * @throws ClassNotFoundException if no such Class found
	 */
	public static Class type2Wrapper(String type) throws ClassNotFoundException {
		switch (type) {
			case "boolean": return Boolean.class; 
			case "byte": return Byte.class; 
			case "char": return Character.class; 
			case "double": return Double.class; 
			case "float": return Float.class; 
			case "int": return Integer.class; 
			case "long": return Long.class; 
			case "short": return Short.class; 
		}
		return Class.forName(type);
	}

	/**
	 * Constructs a new instance of the Class provided. Uses constructor with one String parameter for this.
	 * 
	 * @param cls the class to construct instance of
	 * @param value the initial instance value
	 * @return the created Class instance
	 * @throws Exception in case of any construction error
	 */
	public static Object getValue(Class<?> cls, String value) throws Exception {
		Constructor<?> c = cls.getConstructor(String.class);
		return c.newInstance(value);
	}
	
}
