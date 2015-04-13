package com.bagri.xdm.client.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.idgen.SimpleIdGenerator;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class XDMJsonParser {
	
	private static final Logger logger = LoggerFactory.getLogger(XDMJsonParser.class);

	private static XMLInputFactory factory = XMLInputFactory.newInstance();
	private StringBuilder chars;
	private List<JsonToken> firstTokens;
	private List<XDMData> dataList;
	private Stack<XDMData> dataStack;
	private XDMModelManagement dict;
	private int docType = -1;
	private long elementId;

	public static List<XDMElement> parseDocument(XDMModelManagement dictionary, String xml) throws IOException {
		XDMJsonParser parser = new XDMJsonParser(dictionary);
		return parser.parse(xml);
	}
	
	public XDMJsonParser(XDMModelManagement dict) {
		this.dict = dict;
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
/*	
	public List<XDMData> parse(XMLEventReader eventReader) throws IOException, XMLStreamException {
		
		init();
		while (eventReader.hasNext()) {
			processEvent(eventReader.nextEvent());
		}

		List<XDMData> result = dataList;
		dataList = null;
		return result;
	}
*/	
	public List<XDMElement> parse(JsonParser parser) throws IOException { //, XMLStreamException {

		logger.trace("parse.enter; got parser: {}; context: {}", parser, parser.getParsingContext());
		
		while (parser.nextToken() != null) {
			JsonToken token = parser.getCurrentToken();
			logger.trace("parse; next token: {}; name: {}; text: {}", token, parser.getCurrentName(), parser.getText());
			
			switch (token) {
			
				case START_OBJECT:
					if (dataStack.size() == 0) {
						processDocument(token);
					} else {
						processStartElement(token);
					}
					break;
				case START_ARRAY: 
				case NOT_AVAILABLE:
				case FIELD_NAME:
					break;
				case END_OBJECT:
					processEndElement(token);
					break;
				case END_ARRAY: 
				case VALUE_EMBEDDED_OBJECT:
				case VALUE_FALSE:
				case VALUE_NULL:
				case VALUE_NUMBER_FLOAT:
				case VALUE_NUMBER_INT:
				case VALUE_TRUE:
				case VALUE_STRING:
					processValue(token.asString());
					break;
				default: 
					logger.trace("parse; unknown token: {}", token);
			}
			
		}			
		logger.trace("parse.exit; context: {}; schema: {}", parser.getParsingContext(), parser.getSchema());
		parser.close();
		
		return null;
	}

	private void cleanup() {
		chars = null;
		firstTokens = null;
		dataStack = null;
	}

	private void init() {
		firstTokens = new ArrayList<JsonToken>();
		dataList = new ArrayList<XDMData>();
		dataStack = new Stack<XDMData>();
		docType = -1;
		elementId = 0;
		chars = new StringBuilder();
	}
	
	private void processDocument(JsonToken document) {

		//logger.trace("document: {}", document);
		XDMElement start = new XDMElement();
		start.setElementId(elementId++);
		//start.setParentId(0); // -1 ?
		XDMPath path = dict.translatePath(docType, "", XDMNodeKind.document);
		XDMData data = new XDMData(path, start);
		dataStack.add(data);
		dataList.add(data);
	}

	@SuppressWarnings("unchecked")
	private void processStartElement(JsonToken element) {
		
		XDMData parent = dataStack.peek();
		XDMData current = addData(parent, XDMNodeKind.element, "/" + element.name(), null); 
		dataStack.add(current);

	}

	private void processEndElement(JsonToken element) {

		XDMData current = dataStack.pop();
		if (chars.length() > 0) {
			String content = chars.toString();
			// normalize xml content.. what if it is already normalized??
			content = content.replaceAll("&", "&amp;");
			// trim left/right ? this is schema-dependent. trim if schema-type 
			// is xs:token, for instance..
			XDMData text = addData(current, XDMNodeKind.text, "/text()", content); 
			chars.delete(0, chars.length());
			//logger.trace("text: {}", text);
		}
	}
	
	private void processValue(String value) {

		//if (characters.getData().trim().length() > 0) {
			chars.append(value);
		//}
	}
	
	private XDMData addData(XDMData parent, XDMNodeKind kind, String name, String value) {

		XDMElement xElt = new XDMElement();
		xElt.setElementId(elementId++);
		xElt.setParentId(parent.getElementId());
		String path = parent.getPath() + name;
		xElt.setValue(value);
		XDMPath xPath = dict.translatePath(docType, path, kind);
		XDMData xData = new XDMData(xPath, xElt);
		dataList.add(xData);
		return xData;
	}
}
