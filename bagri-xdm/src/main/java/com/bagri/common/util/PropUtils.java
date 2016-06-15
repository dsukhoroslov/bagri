package com.bagri.common.util;

import static com.bagri.common.util.PropUtils.setProperty;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

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
	
	public static String getProperty(Properties props, String name, String fallback) {
		if (props != null) {
			return props.getProperty(name, fallback);
		}
		return fallback;
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
	
	public static Properties getOutputProperties(Properties source) {
		Properties outProps = new Properties();
		setProperty(source, outProps, OutputKeys.CDATA_SECTION_ELEMENTS, null);
		setProperty(source, outProps, OutputKeys.DOCTYPE_PUBLIC, null);
		setProperty(source, outProps, OutputKeys.DOCTYPE_SYSTEM, null);
		setProperty(source, outProps, OutputKeys.ENCODING, null);
		setProperty(source, outProps, OutputKeys.INDENT, null);
		setProperty(source, outProps, OutputKeys.MEDIA_TYPE, null);
		setProperty(source, outProps, OutputKeys.METHOD, null);
		setProperty(source, outProps, OutputKeys.OMIT_XML_DECLARATION, null);
		setProperty(source, outProps, OutputKeys.STANDALONE, null);
		setProperty(source, outProps, OutputKeys.VERSION, null);
		return outProps;
	}
}
