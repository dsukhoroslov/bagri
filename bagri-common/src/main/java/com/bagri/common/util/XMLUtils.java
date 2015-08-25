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

import static com.bagri.common.util.FileUtils.def_encoding;

public class XMLUtils {
	
	private static final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();  
	private static final XMLInputFactory xiFactory = XMLInputFactory.newInstance();
	private static final TransformerFactory transFactory = TransformerFactory.newInstance();  

	static {
		dbFactory.setNamespaceAware(true);
	}

	public static String textToString(Reader text) throws IOException {
		if (text == null) {
			throw new IOException("Provided reader is null");
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(text);
		String NL = System.getProperty("line.separator");
		String line;
		try {
			while((line = br.readLine()) != null) {
				sb.append(line).append(NL);
            }
			sb.deleteCharAt(sb.length() - 1);
		} finally {
			br.close();
		}
		return sb.toString();
	}

	public static String textToString(InputStream text) throws IOException {
		if (text == null) {
			throw new IOException("Provided stream is null");
		}
		Reader r = new InputStreamReader(text);
		try {
			return textToString(r);
		} finally {
			r.close();
		}
	}
	
	public static Document textToDocument(String text) throws IOException {
		
		try {
			DocumentBuilder builder = dbFactory.newDocumentBuilder();
	        //return builder.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));  
	        return builder.parse(new ByteArrayInputStream(text.getBytes(def_encoding)));  
		} catch (ParserConfigurationException | SAXException ex) {
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
		Reader reader = new StringReader(content);
	    try {
			return xiFactory.createXMLStreamReader(reader);
		} catch (XMLStreamException ex) {
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
			writer.flush();
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

}
