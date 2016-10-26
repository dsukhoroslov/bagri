package com.bagri.common.util;

import static com.bagri.common.util.FileUtils.EOL;
import static java.util.Calendar.*;
import static javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED;
import static javax.xml.xquery.XQItemType.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import static com.bagri.common.util.FileUtils.def_encoding;

/**
 * A set of static utility methods working with XML
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XMLUtils {

	private static final DatatypeFactory dtFactory;
	private static final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();  
	private static final TransformerFactory transFactory = TransformerFactory.newInstance();  
	private static final XMLInputFactory xiFactory = XMLInputFactory.newInstance();
	//private static final XStream xStream = new XStream(new DomDriver(def_encoding, new NoNameCoder()));
	private static final XStream xStream = new XStream(new StaxDriver(new NoNameCoder()));
	
	static {
		dbFactory.setNamespaceAware(true);
		xStream.alias("map", java.util.Map.class);
		xStream.registerConverter(new MapConverter());
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException("Can not instantiate datatype factory");
		}
	}

	private static final ThreadLocal<DocumentBuilder> thDB = new ThreadLocal<DocumentBuilder>() {
		
		@Override
		protected DocumentBuilder initialValue() {
			try {
				return dbFactory.newDocumentBuilder();
			} catch (ParserConfigurationException ex) {
				throw new RuntimeException(ex);
			}
 		}

		@Override
		public DocumentBuilder get() {
			DocumentBuilder result = super.get();
			result.reset();
			return result;
 		}
		
	};
	
	private static final ThreadLocal<Transformer> thTR = new ThreadLocal<Transformer>() {
		
		@Override
		protected Transformer initialValue() {
			try {
				return transFactory.newTransformer();
			} catch (TransformerConfigurationException ex) {
				throw new RuntimeException(ex);
			}
 		}
		
		@Override
		public Transformer get() {
			Transformer result = super.get();
			result.reset();
			return result;
 		}
	};
	
	/**
	 * converts {@link GregorianCalendar} to the corresponding {@link XMLGregorianCalendar} instance 
	 * 
	 * @param gc the initial GregorianCalendar instance
	 * @param cType one of XQJ base type constants 
	 * @return XML gregorian calendar instance
	 */
	public static XMLGregorianCalendar getXMLCalendar(GregorianCalendar gc, int cType) { 
    	switch (cType) {
    		case XQBASETYPE_DATE:
    			return dtFactory.newXMLGregorianCalendarDate(gc.get(YEAR), 
    					gc.get(MONTH) + 1, gc.get(DAY_OF_MONTH), gc.get(ZONE_OFFSET)); 
    		case XQBASETYPE_GDAY: 
    			return dtFactory.newXMLGregorianCalendarDate(FIELD_UNDEFINED, 
    					FIELD_UNDEFINED, gc.get(DAY_OF_MONTH), FIELD_UNDEFINED); 
    		case XQBASETYPE_GMONTH:  
    			return dtFactory.newXMLGregorianCalendarDate(FIELD_UNDEFINED, 
    					gc.get(MONTH) + 1, FIELD_UNDEFINED, FIELD_UNDEFINED); 
    		case XQBASETYPE_GMONTHDAY:  
    			return dtFactory.newXMLGregorianCalendarDate(FIELD_UNDEFINED, 
    					gc.get(MONTH) + 1, gc.get(DAY_OF_MONTH), FIELD_UNDEFINED); 
    		case XQBASETYPE_GYEAR:  
    			return dtFactory.newXMLGregorianCalendarDate(gc.get(YEAR), 
    					FIELD_UNDEFINED, FIELD_UNDEFINED, FIELD_UNDEFINED); 
    		case XQBASETYPE_GYEARMONTH: 
    			return dtFactory.newXMLGregorianCalendarDate(gc.get(YEAR), 
    					gc.get(Calendar.MONTH) + 1, FIELD_UNDEFINED, FIELD_UNDEFINED); 
    		case XQBASETYPE_TIME:
    			return dtFactory.newXMLGregorianCalendarTime(gc.get(HOUR), gc.get(MINUTE), 
    					gc.get(SECOND), gc.get(MILLISECOND), gc.get(ZONE_OFFSET)); 
    		//default: //XQBASETYPE_DATETIME 
    	}
    	return dtFactory.newXMLGregorianCalendar(gc);
    }

	public static XMLGregorianCalendar newXMLCalendar(String value) {
		return dtFactory.newXMLGregorianCalendar(value);
	}
	
	/**
	 * converts String representation of duration to its XML equivalent.
	 * Returns null if the type provided does not correspond to any XML duration types.
	 * 
	 * @param duration the String duration representation
	 * @param dType one of XQJ base type constants 
	 * @return XML {@link Duration} instance or null
	 */
    public static Duration getXMLDuration(String duration, int dType) { 
    	switch (dType) {
			case XQBASETYPE_DURATION: return dtFactory.newDuration(duration); 
			case XQBASETYPE_DAYTIMEDURATION: return dtFactory.newDurationDayTime(duration); 
			case XQBASETYPE_YEARMONTHDURATION: return dtFactory.newDurationYearMonth(duration);
    	}
    	return null;
    }
	
	
	/**
	 * Reads content from Reader and return it as String
	 * 
	 * @param text the Reader to read from
	 * @return the String result
	 * @throws IOException in case of read error
	 */
	public static String textToString(Reader text) throws IOException {
		if (text == null) {
			throw new IOException("Provided reader is null");
		}
		String line;
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(text)) {
			while((line = br.readLine()) != null) {
				sb.append(line).append(EOL);
            }
			sb.delete(sb.length() - EOL.length(), sb.length());
		}
		return sb.toString();
	}

	/**
	 * Reads content from InputStream and return it as String
	 * 
	 * @param text the InputStream to read from
	 * @return the String result
	 * @throws IOException in case of read error
	 */
	public static String textToString(InputStream text) throws IOException {
		if (text == null) {
			throw new IOException("Provided stream is null");
		}
		try (Reader r = new InputStreamReader(text)) {
			return textToString(r);
		}
	}
	
	/**
	 * Produce new XML Document from the content provided as String 
	 * 
	 * @param text the content to put into the Document
	 * @return the XML Document
	 * @throws IOException in case of XML processing error
	 */
	public static Document textToDocument(String text) throws IOException {
		DocumentBuilder builder = thDB.get();
		try {
	        return builder.parse(new ByteArrayInputStream(text.getBytes(def_encoding)));  
	        // shouldn't we close IS above?
		} catch (SAXException ex) {
			throw new IOException(ex); 
		}  
	}

	/**
	 * Produce new XML Document from the content provided as InputStream 
	 * 
	 * @param text the content stream to put into the Document
	 * @return the XML Document
	 * @throws IOException in case of XML processing error
	 */
	public static Document textToDocument(InputStream text) throws IOException {
		DocumentBuilder builder = thDB.get();
		try {
	        return builder.parse(text);  
		} catch (SAXException ex) {
			throw new IOException(ex); 
		}  
	}

	/**
	 * Produce new XML Document from the content provided as Reader
	 * 
	 * @param text the content reader to put into the Document
	 * @return the XML Document
	 * @throws IOException in case of XML processing error
	 */
	public static Document textToDocument(Reader text) throws IOException {
		DocumentBuilder builder = thDB.get();
		try {
	        return builder.parse(new InputSource(text));  
		} catch (SAXException ex) {
			throw new IOException(ex); 
		}  
	}
	
	/**
	 * Creates an XMLStreamReader over the content provided as String
	 * 
	 * @param content the String content to parse
	 * @return the streaming reader over the content 
	 * @throws IOException in case of reader creation error
	 */
	public static XMLStreamReader stringToStream(String content) throws IOException {
		//get Reader connected to XML input from somewhere..?
		// note: we can not close this reader as it is used further
		Reader reader = new StringReader(content);
	    try {
			return xiFactory.createXMLStreamReader(reader);
		} catch (XMLStreamException ex) {
			reader.close();
			throw new IOException(ex); 
		}
	}
	
	/**
	 * Transforms XML Source to XML String representation
	 * 
	 * @param source the XML Source 
	 * @param props XML output properties as {@link OutputKeys}
	 * @return XML String
	 * @throws IOException in case of XML processing error
	 */
	public static String sourceToString(Source source, Properties props) throws IOException { 
		Transformer trans = thTR.get();
		try {
			if (props != null) {
				trans.setOutputProperties(props);
			} else {
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				trans.setOutputProperty(OutputKeys.INDENT, "yes");
			}
			Writer writer = new StringWriter();
			trans.transform(source, new StreamResult(writer));
			writer.close();
			return writer.toString();
		} catch (TransformerException ex) {
			throw new IOException(ex); 
		}  
	}
	
	/**
	 * Transforms a particular XML Node to XML String representation
	 * 
	 * @param node the XML Node to marshal 
	 * @param props XML output properties as {@link OutputKeys}
	 * @return XML String
	 * @throws IOException in case of XML processing error
	 */
	public static String nodeToString(Node node, Properties props) throws IOException {
		return sourceToString(new DOMSource(node), props);
	}
	
	/**
	 * Transforms XML String content to XML Result
	 * 
	 * @param source the String XML content
	 * @param result the Result to transform to
	 * @throws IOException in case of XML processing error
	 */
	public static void stringToResult(String source, Result result) throws IOException {
		Transformer trans = thTR.get();
		try {
			StringReader reader = new StringReader(source);
			trans.transform(new StreamSource(reader), result);
		} catch (TransformerException ex) {
			throw new IOException(ex); 
		}  
	}
	
	/**
	 * Serialize POJO to XML string
	 * 
	 * @param bean the POJO to serialize
	 * @return the serialization result
	 */
	public static String beanToXML(Object bean) { 
		return xStream.toXML(bean);
	}

	/**
	 * Deserialize POJO from XML string
	 * 
	 * @param xml the XML to deserialize
	 * @return the deserialization result
	 */
	public static Object beanFromXML(String xml) { 
		return xStream.fromXML(xml);		
	}
	
	/**
	 * Serialize Map to XML string
	 * 
	 * @param map the Map to serialize
	 * @return the serialization result
	 */
	public static String mapToXML(Map<String, Object> map) {
		return xStream.toXML(map);
	}
	
	/**
	 * Deserialize Map from XML string
	 * 
	 * @param xml the XML to deserialize
	 * @return the deserialization result
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> mapFromXML(String xml) {
		return (Map<String, Object>) xStream.fromXML(xml);		
	}


	private static class MapConverter implements Converter {
		
		private ConcurrentHashMap<String, Class<?>> types = new ConcurrentHashMap<>();
	
	    @SuppressWarnings("rawtypes")
		public boolean canConvert(Class clazz) {
	        return Map.class.isAssignableFrom(clazz);
	    }
	
	    @SuppressWarnings("unchecked")
		public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
	
	        Map<String, Object> map = (Map<String, Object>) value;
	        for (Map.Entry<String, Object> entry : map.entrySet()) {
	            writer.startNode(entry.getKey());
	            Object val = entry.getValue();
	            if (val != null) {
	            	types.putIfAbsent(entry.getKey(), val.getClass());
	            	context.convertAnother(val);
	            }
	            writer.endNode();
	        }
	    }
	
	    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
	
	        Map<String, Object> map = new HashMap<>();
	        while (reader.hasMoreChildren()) {
	            reader.moveDown();
	            String key = reader.getNodeName(); 
	            Object value = context.convertAnother(map, types.get(key));
	            map.put(key, value);
	            reader.moveUp();
	        }
	        return map;
	    }
	
	}

}