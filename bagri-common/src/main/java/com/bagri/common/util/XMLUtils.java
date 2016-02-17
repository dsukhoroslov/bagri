package com.bagri.common.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

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
import com.thoughtworks.xstream.io.xml.StaxDriver;

import static com.bagri.common.util.FileUtils.def_encoding;

public class XMLUtils {

	private static final String EOL = System.getProperty("line.separator");
	
	private static final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();  
	private static final TransformerFactory transFactory = TransformerFactory.newInstance();  
	private static final XMLInputFactory xiFactory = XMLInputFactory.newInstance();
	private static final XStream xStream = new XStream(new StaxDriver());
	
	static {
		dbFactory.setNamespaceAware(true);
		xStream.alias("map", java.util.Map.class);
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
		
	};
	
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
			sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}

	public static String textToString(InputStream text) throws IOException {
		if (text == null) {
			throw new IOException("Provided stream is null");
		}
		try (Reader r = new InputStreamReader(text)) {
			return textToString(r);
		}
	}
	
	public static Document textToDocument(String text) throws IOException {
		DocumentBuilder builder = thDB.get();
		try {
	        return builder.parse(new ByteArrayInputStream(text.getBytes(def_encoding)));  
	        // shouldn't we close IS above?
		} catch (SAXException ex) {
			throw new IOException(ex); 
		}  
	}

	public static Document textToDocument(InputStream text) throws IOException {
		try {
			DocumentBuilder builder = dbFactory.newDocumentBuilder();
	        return builder.parse(text);  
		} catch (ParserConfigurationException | SAXException ex) {
			throw new IOException(ex); 
		}  
	}

	public static Document textToDocument(Reader text) throws IOException {
		try {
			DocumentBuilder builder = dbFactory.newDocumentBuilder();
	        return builder.parse(new InputSource(text));  
		} catch (ParserConfigurationException | SAXException ex) {
			throw new IOException(ex); 
		}  
	}
	
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
	
	public static String sourceToString(Source source) throws IOException { 
		try {
			Transformer trans = transFactory.newTransformer();
	    	trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    	trans.setOutputProperty(OutputKeys.INDENT, "yes");
			Writer writer = new StringWriter();
			trans.transform(source, new StreamResult(writer));
			writer.close();
			return writer.toString();
		} catch (TransformerException ex) {
			throw new IOException(ex); 
		}  
	}
	
	public static String nodeToString(Node node) throws IOException {
		return sourceToString(new DOMSource(node));
	}
	
	public static void stringToResult(String source, Result result) throws IOException {
		try {
			Transformer trans = transFactory.newTransformer();  
			StringReader reader = new StringReader(source);
			trans.transform(new StreamSource(reader), result);
		} catch (TransformerException ex) {
			throw new IOException(ex); 
		}  
	}
	
	public static String beanToXML(Object bean) throws IOException {
		return xStream.toXML(bean);
	}

	public static Object beanFromXML(String xml) throws IOException {
		return xStream.fromXML(xml);		
	}
	
	public static String mapToXML(Map<String, Object> map) {
		return xStream.toXML(map);
	}
	
	public static Map<String, Object> mapFromXML(String xml) {
		return (Map<String, Object>) xStream.fromXML(xml);		
	}

}
