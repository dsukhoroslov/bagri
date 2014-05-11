package com.bagri.xdm.access.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.idgen.SimpleIdGenerator;
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.domain.XDMElement;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class XDMJsonParser {
	
	private static final Logger logger = LoggerFactory.getLogger(XDMJsonParser.class);

	private static XMLInputFactory factory = XMLInputFactory.newInstance();
	private StringBuilder chars;
	private List<XDMElement> dataList;

	//private long elementId = 0;
	private long documentId = 0;
	private Stack<XDMElement> dataStack;
	private IdGenerator<Long> idGen;
	private XDMSchemaDictionary dict;

	public static List<XDMElement> parseDocument(XDMSchemaDictionary dictionary, long documentId, 
			String xml) throws IOException, XMLStreamException {
		XDMJsonParser parser = new XDMJsonParser(dictionary, documentId);
		return parser.parse(xml);
	}
	
	public XDMJsonParser() {
		//this(0);
	}
	
	public XDMJsonParser(XDMSchemaDictionary dict, long documentId) {
		this.dict = dict;
		this.documentId = documentId;
		this.idGen = new SimpleIdGenerator(0);
	}

	public XDMJsonParser(XDMSchemaDictionary dict, long documentId, IdGenerator generator) {
		this.dict = dict;
		this.documentId = documentId;
		this.idGen = generator;
	}

	public List<XDMElement> parse(String json) throws IOException { //, XMLStreamException {
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createParser(json);	
		return parse(jParser);
	}
	
	public List<XDMElement> parse(File file) throws IOException { //, XMLStreamException {
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createParser(file);	
		return parse(jParser);
	}
	
	public List<XDMElement> parse(InputStream stream) throws IOException { //, XMLStreamException {
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createParser(stream);	
		return parse(jParser);
	}
	
	public List<XDMElement> parse(Reader reader) throws IOException { //, XMLStreamException {
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createParser(reader);	
		return parse(jParser);
	}
	
	public List<XDMElement> parse(JsonParser parser) throws IOException { //, XMLStreamException {

		logger.trace("parse.enter; got parser: {}; context: {}", parser, parser.getParsingContext());
		
		while (parser.nextToken() != null) {
			JsonToken token = parser.getCurrentToken();
			logger.trace("parse; next token: {}; name: {}; text: {}", token, parser.getCurrentName(), parser.getText());
			
			switch (token) {
			
				case START_ARRAY: 
				case START_OBJECT:
				case NOT_AVAILABLE:
				case FIELD_NAME:  
				case END_OBJECT: 
				case END_ARRAY: 
				case VALUE_EMBEDDED_OBJECT:
				case VALUE_FALSE:
				case VALUE_NULL:
				case VALUE_NUMBER_FLOAT:
				case VALUE_NUMBER_INT:
				case VALUE_TRUE:
				case VALUE_STRING:
					//parser.
					break;
				default: 
					logger.trace("parse; unknown token: {}", token);
			}
			
		}			
		logger.trace("parse.exit; context: {}; schema: {}", parser.getParsingContext(), parser.getSchema());
		parser.close();
		
		return null;
	}

}
