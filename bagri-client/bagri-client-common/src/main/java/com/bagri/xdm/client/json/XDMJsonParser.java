package com.bagri.xdm.client.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
import com.bagri.xdm.domain.XDMParser;
import com.bagri.xdm.domain.XDMPath;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class XDMJsonParser implements XDMParser {
	
	private static final Logger logger = LoggerFactory.getLogger(XDMJsonParser.class);

	private static JsonFactory factory = new JsonFactory();

	private List<XDMData> dataList;
	private Stack<XDMData> dataStack;
	private XDMModelManagement dict;
	private int docType = -1;
	private long elementId;

	public static List<XDMData> parseDocument(XDMModelManagement dictionary, String json) throws IOException {
		XDMJsonParser parser = new XDMJsonParser(dictionary);
		return parser.parse(json);
	}
	
	public XDMJsonParser(XDMModelManagement dict) {
		this.dict = dict;
	}

	@Override
	public List<XDMData> parse(String json) throws IOException { 
		try (Reader reader = new StringReader(json)) {
			return parse(reader);
		}
	}
	
	@Override
	public List<XDMData> parse(File file) throws IOException {
		try (Reader reader = new FileReader(file)) {
			return parse(reader);
		}
	}
	
	@Override
	public List<XDMData> parse(InputStream stream) throws IOException {
		
		JsonParser jParser = null;
		try {
			jParser = factory.createParser(stream);	
			return parse(jParser);
		} finally {
			if (jParser != null) {
				jParser.close();
			}
		}
	}
	
	@Override
	public List<XDMData> parse(Reader reader) throws IOException {
		
		JsonParser jParser = null;
		try {
			jParser = factory.createParser(reader);	
			return parse(jParser);
		} finally {
			if (jParser != null) {
				jParser.close();
			}
		}
	}

	public List<XDMData> parse(JsonParser parser) throws IOException {
		
		logger.trace("parse.enter; context: {}; schema: {}", parser.getParsingContext(), parser.getSchema());
		
		init();
		while (parser.nextToken() != null) {
			processToken(parser);
		}

		List<XDMData> result = dataList;
		dataList = null;
		logger.trace("parse.exit; returning {} elements", result); //.size());
		return result;
	}
	
	public void processToken(JsonParser parser) throws IOException { //, XMLStreamException {

		JsonToken token = parser.getCurrentToken();
		logger.trace("processToken; got token: {}; name: {}; value: {}", token.name(), parser.getCurrentName(), parser.getText());
		
		switch (token) {
			
			case START_OBJECT:
				if (dataStack.size() == 0) {
					processDocument(parser.nextFieldName());
				} 
			case START_ARRAY: 
				if (parser.getCurrentName() != null) {
					processStartElement(parser.getCurrentName());
				}
			case NOT_AVAILABLE:
			case FIELD_NAME:
				break;
			case END_OBJECT:
			case END_ARRAY: 
				if (parser.getCurrentName() != null) {
					processEndElement();
				}
				break;
			case VALUE_EMBEDDED_OBJECT:
			case VALUE_FALSE:
			case VALUE_NULL:
			case VALUE_NUMBER_FLOAT:
			case VALUE_NUMBER_INT:
			case VALUE_TRUE:
			case VALUE_STRING:
				processStartElement(parser.getCurrentName());
				processValueElement(parser.getText());
				break;
			default: 
				logger.trace("parse; unknown token: {}", token);
		}			
	}

	private void cleanup() {
		dataStack = null;
	}

	private void init() {
		dataList = new ArrayList<XDMData>();
		dataStack = new Stack<XDMData>();
		docType = -1;
		elementId = 0;
	}
	
	private void processDocument(String name) {

		XDMElement start = new XDMElement();
		start.setElementId(elementId++);
		//start.setParentId(0); // -1 ?
		String root = "/" + (name == null ? "" : name);
		docType = dict.translateDocumentType(root);
		XDMPath path = getPath("", XDMNodeKind.document);
		XDMData data = new XDMData(path, start);
		dataStack.add(data);
		dataList.add(data);
	}

	@SuppressWarnings("unchecked")
	private void processStartElement(String name) {
		
		XDMData parent = dataStack.peek();
		if (!name.equals(parent.getName())) {
			XDMData current = addData(parent, XDMNodeKind.element, "/" + name, null); 
			dataStack.add(current);
		}
	}

	private void processEndElement() {

		dataStack.pop();
	}

	private void processValueElement(String value) {
		
		XDMData current = dataStack.pop();
		//String content = value.replaceAll("&", "&amp;");
		addData(current, XDMNodeKind.text, "/text()", value);
	}
	
	private XDMData addData(XDMData parent, XDMNodeKind kind, String name, String value) {

		XDMElement xElt = new XDMElement();
		xElt.setElementId(elementId++);
		xElt.setParentId(parent.getElementId());
		String path = parent.getPath() + name;
		xElt.setValue(value);
		XDMPath xPath = getPath(path, kind);
		XDMData xData = new XDMData(xPath, xElt);
		dataList.add(xData);
		return xData;
	}
	
	private XDMPath getPath(String path, XDMNodeKind kind) {
		return dict.translatePath(docType, path, kind);
		//return new XDMPath(path, 0, kind, 0, 0, 0);
	}
}
