package com.bagri.common.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class PropUtils {
	
	private static final String EOL = System.getProperty("line.separator");

	public static Properties propsFromFile(String fileName) throws IOException {
		Properties props = new Properties();
		props.load(new FileReader(fileName));
		return props;
	}
	
	public static Properties propsFromString(String properties) throws IOException {
		Properties props = new Properties();
		properties = properties.replaceAll(";", EOL);
		props.load(new StringReader(properties));
		return props;
	}
	
	public static String getSystemProperty(String name, String fallback) {
		String prop = System.getProperty(name);
		if (prop == null) {
			prop = System.getProperty(fallback);
			//if (prop == null) {
			//	prop = fallback;
			//}
		}
		return prop;
	}
	
	public static void setProperty(Properties source, Properties target, String name, String fallback) {
		String prop = source.getProperty(name);
		if (prop == null && fallback != null) {
			prop = source.getProperty(fallback);
		}
		
		if (prop != null) {
			target.setProperty(name, prop);
		}
	}
	
	public static void setProperty(Properties target, String name, String fallback) {
		String prop = System.getProperty(name);
		if (prop == null) {
			prop = fallback;
		}
		
		if (prop != null) {
			target.setProperty(name, prop);
		}
	}
	
}
