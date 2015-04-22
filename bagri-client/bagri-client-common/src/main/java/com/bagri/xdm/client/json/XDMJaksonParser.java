package com.bagri.xdm.client.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.client.parser.XDMDataParser;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMParser;
import com.bagri.xdm.domain.XDMPath;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class XDMJaksonParser extends XDMDataParser implements XDMParser {
	
	private static JsonFactory factory = new JsonFactory();

	public static List<XDMData> parseDocument(XDMModelManagement dictionary, String json) throws IOException {
		XDMJaksonParser parser = new XDMJaksonParser(dictionary);
		return parser.parse(json);
	}
	
	public XDMJaksonParser(XDMModelManagement dict) {
		super(dict);
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
		cleanup();

		List<XDMData> result = dataList;
		dataList = null;
		logger.trace("parse.exit; returning {} elements", result); //.size());
		return result;
	}
	
	private void processToken(JsonParser parser) throws IOException { //, XMLStreamException {

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
				processValueElement(parser.getCurrentName(), parser.getText());
				break;
			default: 
				logger.trace("processToken; unknown token: {}", token);
		}			
	}

	private void processDocument(String name) {

		XDMElement start = new XDMElement();
		start.setElementId(elementId++);
		//start.setParentId(0); // -1 ?
		String root = "/" + (name == null ? "" : name);
		docType = dict.translateDocumentType(root);
		XDMPath path = dict.translatePath(docType, "", XDMNodeKind.document); 
		XDMData data = new XDMData(path, start);
		dataStack.add(data);
		dataList.add(data);
	}
	
	private boolean isAttribute(String name) {
		return name.startsWith("-") || name.startsWith("@");
	}

	private void processStartElement(String name) {
		
		if (name != null && !isAttribute(name)) {
			XDMData parent = dataStack.peek();
			if (!name.equals(parent.getName())) {
				XDMData current = addData(parent, XDMNodeKind.element, "/" + name, null); 
				dataStack.add(current);
			}
		}
	}

	private void processEndElement() {

		dataStack.pop();
	}

	private void processValueElement(String name, String value) {
		
		//String content = value.replaceAll("&", "&amp;");
		if (name == null) {
			XDMData current = dataStack.peek();
			addData(current, XDMNodeKind.text, "/text()", value);
		} else if (isAttribute(name)) {
			XDMData current = dataStack.peek(); 
			name = name.substring(1);
			if (name.startsWith("xmlns")) {
				addData(current, XDMNodeKind.namespace, "/#" + name, value);
			} else {
				addData(current, XDMNodeKind.attribute, "/@" + name, value);
			}
		} else {
			XDMData current = dataStack.pop();
			addData(current, XDMNodeKind.text, "/text()", value);
		}
	}
	
}
