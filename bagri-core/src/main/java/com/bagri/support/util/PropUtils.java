package com.bagri.support.util;

import static com.bagri.support.util.FileUtils.EOL;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

/**
 * A set of static utility methods for Properties
 * 
 * @author Denis Sukhoroslov
 *
 */
public class PropUtils {
	
	/**
	 * Load Properties from file
	 * 
	 * @param fileName the file name to load Properties from
	 * @return Properties loaded from file
	 * @throws IOException in case of load error
	 */
	public static Properties propsFromFile(String fileName) throws IOException {
		Properties props = new Properties();
		props.load(new FileReader(fileName));
		return props;
	}
	
	/**
	 * Load Properties from string. Replaces ";" separators with new line separators
	 * 
	 * @param properties the string containing properties. Properties can be separated by ";" symbol
	 * @return Properties loaded from String 
	 * @throws IOException in case of load error
	 */
	public static Properties propsFromString(String properties) throws IOException {
		if (properties == null) {
			return null;
		}
		Properties props = new Properties();
		properties = properties.replaceAll(";", EOL);
		props.load(new StringReader(properties));
		return props;
	}
	
	/**
	 * Return property value if it is found in the Properties passed, or default value
	 * 
	 * @param props the Properties to lookup
	 * @param name the property name to look for
	 * @param fallback the property default value
	 * @return the property value if found, or default value otherwise
	 */
	public static String getProperty(Properties props, String name, String fallback) {
		if (props != null) {
			return props.getProperty(name, fallback);
		}
		return fallback;
	}
	
	/**
	 * Return system property for the name provided. If property not found returns property value for the fallback property passed 
	 * 
	 * @param name the property name to look for
	 * @param fallback the second property name to use if no property found for the first name
	 * @return the property value if found, or null
	 */
	public static String getSystemProperty(String name, String fallback) {
		String prop = System.getProperty(name);
		if (prop == null) {
			prop = System.getProperty(fallback);
		}
		return prop;
	}
	
	/**
	 * Takes property from source and set it in target Properties
	 * 
	 * @param source the source Properties to take property value from
	 * @param target the target Properties to set property value in
	 * @param name the property name
	 * @param fallback the second property name if first one not found
	 */
	public static void setProperty(Properties source, Properties target, String name, String fallback) {
		String prop = source.getProperty(name);
		if (prop == null && fallback != null) {
			prop = source.getProperty(fallback);
		}
		
		if (prop != null) {
			target.setProperty(name, prop);
		}
	}
	
	/**
	 * Takes property from System and set it in target Properties
	 * 
	 * @param target the target Properties to set property value in
	 * @param name the property name
	 * @param fallback the second property name if first one not found
	 */
	public static void setProperty(Properties target, String name, String fallback) {
		String prop = System.getProperty(name);
		if (prop == null) {
			prop = fallback;
		}
		
		if (prop != null) {
			target.setProperty(name, prop);
		}
	}

	/**
	 * Takes property from source and set it in target Properties. Set default value if no property found in source
	 * 
	 * @param source the source Properties to take property value from
	 * @param target the target Properties to set property value in
	 * @param name the property name
	 * @param defValue default value to set if no property found in source
	 */
	public static void setPropertyOrDefault(Properties source, Properties target, String name, String defValue) {
		String prop = source.getProperty(name);
		if (prop != null) {
			target.setProperty(name, prop);
		} else {
			target.setProperty(name, defValue);
		}
	}
	
	/**
	 * Extract XML-output specific properties from the wider list of Properties passed 
	 * 
	 * @param source the source properties list
	 * @return the XML-output specific set of properties
	 */
	public static Properties getOutputProperties(Properties source) {
		Properties outProps = new Properties();
		setProperty(source, outProps, OutputKeys.CDATA_SECTION_ELEMENTS, null);
		setProperty(source, outProps, OutputKeys.DOCTYPE_PUBLIC, null);
		setProperty(source, outProps, OutputKeys.DOCTYPE_SYSTEM, null);
		setProperty(source, outProps, OutputKeys.ENCODING, null);
		setPropertyOrDefault(source, outProps, OutputKeys.INDENT, "yes");
		setProperty(source, outProps, OutputKeys.MEDIA_TYPE, null);
		setPropertyOrDefault(source, outProps, OutputKeys.METHOD, "xml");
		setPropertyOrDefault(source, outProps, OutputKeys.OMIT_XML_DECLARATION, "yes");
		setProperty(source, outProps, OutputKeys.STANDALONE, null);
		setProperty(source, outProps, OutputKeys.VERSION, null);
		return outProps;
	}
}
