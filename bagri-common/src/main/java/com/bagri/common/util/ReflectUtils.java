package com.bagri.common.util;

public class ReflectUtils {

	public static Class type2Class(String type) throws ClassNotFoundException {
		switch (type) {
			case "boolean": return boolean.class; 
			case "byte": return byte.class; 
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
	
}
