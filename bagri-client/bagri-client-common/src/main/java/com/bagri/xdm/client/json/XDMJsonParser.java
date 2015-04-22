package com.bagri.xdm.client.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.client.parser.XDMDataParser;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMParser;
import com.bagri.xdm.domain.XDMPath;

public class XDMJsonParser extends XDMDataParser implements XDMParser {
	
	private static JsonParserFactory factory;
	static {
		//JsonProvider provider = JsonProvider.provider();
		//Map<String, Boolean> config = new HashMap<String, Boolean>();
		//config.put(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES.name(), true);
		//config.put(JsonParser.Feature.ALLOW_COMMENTS.name(), true);
		//config.put(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.name(), true);
		//provider.createParserFactory(config); // Understands JsonFactory and JsonParser features
		
		Map<String, Object> params = new HashMap<String, Object>();
		//params.put("javax.json.spi.JsonProvider", "com.github.pgelinas.jackson.javax.json.spi.JacksonProvider");
		factory = Json.createParserFactory(params);
	}
	
	public static List<XDMData> parseDocument(XDMModelManagement dictionary, String json) throws IOException {
		XDMJsonParser parser = new XDMJsonParser(dictionary);
		return parser.parse(json);
	}
	
	public XDMJsonParser(XDMModelManagement dict) {
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

		try (JsonParser parser = factory.createParser(stream)) {
			return parse(parser);
		}
	}
	
	@Override
	public List<XDMData> parse(Reader reader) throws IOException {
		
		try (JsonParser parser = factory.createParser(reader)) {
			return parse(parser);
		}
	}

	public List<XDMData> parse(JsonParser parser) throws IOException {
		
		logger.trace("parse.enter; parser: {}", parser);
		
		init();
		while (parser.hasNext()) {
			processEvent(parser);
		}
		cleanup();

		List<XDMData> result = dataList;
		dataList = null;
		logger.trace("parse.exit; returning {} elements", result); //.size());
		return result;
	}
	
	private void processEvent(JsonParser parser) throws IOException { //, XMLStreamException {

		JsonParser.Event event = parser.next();
		logger.trace("processEvent; got token: {}; text: {}", event.name()); //, parser.getString());
		
		switch (event) {
			
			case START_OBJECT:
				if (dataStack.size() == 0) {
					parser.next();
					processDocument(parser.getString());
					processStartElement(parser.getString());
				}
				break;
			case START_ARRAY: 
				//if (parser.getString() != null) {
					processStartElement(null);
				//}
				break;
			case KEY_NAME:
				processStartElement(parser.getString());
				break;
			case END_OBJECT:
			case END_ARRAY: 
				//if (parser.getString() != null) {
					processEndElement();
				//}
				break;
			case VALUE_FALSE:
			case VALUE_NULL:
			case VALUE_NUMBER:
			case VALUE_TRUE:
			case VALUE_STRING:
				processValueElement(parser.getString());
				break;
			default: 
				logger.trace("processEvent; unknown event: {}", event);
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
		
		if (name != null) { // && !name.startsWith("-")) {
			XDMData parent = dataStack.peek();
			if (!name.equals(parent.getName())) {
				XDMData current = null;
				if (isAttribute(name)) {
					name = name.substring(1);
					if (name.startsWith("xmlns")) {
						current = addData(parent, XDMNodeKind.namespace, "/#" + name, null);
					} else {
						current = addData(parent, XDMNodeKind.attribute, "/@" + name, null);
					}
				//} else if (name.equals("#text")) {
					// just swallow it
				} else {
					current = addData(parent, XDMNodeKind.element, "/" + name, null); 
				}
				if (current != null) {
					dataStack.add(current);
				}
			}
		} else {
			dataStack.add(null);
		}
	}

	private void processEndElement() {

		dataStack.pop();
	}

	private void processValueElement(String value) {
		
		//XDMData current = dataStack.pop();
		//String content = value.replaceAll("&", "&amp;");
		//addData(current, XDMNodeKind.text, "/text()", value);
	
		XDMData current = dataStack.pop();
		boolean isArray = current == null;
		if (isArray) {
			current = dataStack.peek();
		}
		if (current.getNodeKind() == XDMNodeKind.element) {
			addData(current, XDMNodeKind.text, "/text()", value);
		} else {
			current.getElement().setValue(value);
		}
		if (isArray) {
			dataStack.add(null);
		}
	}	
}
