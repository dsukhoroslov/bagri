package com.bagri.xdm.common.df.json;

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
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParserFactory;
import javax.xml.xquery.XQItemType;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.ContentParser;
import com.bagri.xdm.cache.api.ModelManagement;
import com.bagri.xdm.common.df.ContentParserBase;
import com.bagri.xdm.domain.Occurrence;
import com.bagri.xdm.domain.Data;
import com.bagri.xdm.domain.Element;
import com.bagri.xdm.domain.NodeKind;
import com.bagri.xdm.domain.Path;

/**
 * XDM Parser implementation for JSON data format. Uses reference implementation (Glassfish) of json streaming parser.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class JsonApiParser extends ContentParserBase implements ContentParser {
	
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
	
	/**
	 * 
	 * @param model the model management component
	 * @param json the document content in JSON format
	 * @return the list of parsed XDM data elements
	 * @throws IOException in case of content read exception
	 * @throws XDMException in case of content parse exception
	 */
	public static List<Data> parseDocument(ModelManagement model, String json) throws IOException, XDMException {
		JsonApiParser parser = new JsonApiParser(model);
		return parser.parse(json);
	}
	
	/**
	 * 
	 * @param model the model management component
	 */
	public JsonApiParser(ModelManagement model) {
		super(model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(String json) throws XDMException { 
		try (Reader reader = new StringReader(json)) {
			return parse(reader);
		} catch (IOException ex) {
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(File file) throws XDMException {
		try (Reader reader = new FileReader(file)) {
			return parse(reader);
		} catch (IOException ex) {
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(InputStream stream) throws XDMException {

		try (JsonParser parser = factory.createParser(stream)) {
			return parse(parser);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(Reader reader) throws XDMException {
		
		try (JsonParser parser = factory.createParser(reader)) {
			return parse(parser);
		}
	}

	/**
	 * 
	 * @param parser the JSON streaming parser
	 * @return the list of parsed XDM data elements
	 * @throws XDMException in case of any parsing error
	 */
	public List<Data> parse(JsonParser parser) throws XDMException {
		
		logger.trace("parse.enter; parser: {}", parser);
		
		init();
		while (parser.hasNext()) {
			processEvent(parser);
		}
		cleanup();

		List<Data> result = dataList;
		dataList = null;
		logger.trace("parse.exit; returning {} elements", result); //.size());
		return result;
	}
	
	private void processEvent(JsonParser parser) throws XDMException { //, XMLStreamException {

		JsonParser.Event event = parser.next();
		if (event == Event.VALUE_STRING || event == Event.VALUE_NUMBER) {
			logger.trace("processEvent; got token: {}; value: {}", event.name(), parser.getString());
		} else if (event == Event.KEY_NAME) {
			logger.trace("processEvent; got token: {}; key: {}", event.name(), parser.getString());
		} else {
			logger.trace("processEvent; got token: {}", event.name()); 
		}
		
		switch (event) {
			
			case START_OBJECT:
				if (dataStack.size() == 0) {
					parser.next();
					processDocument(parser.getString());
					processStartElement(parser.getString());
				} else {
					processStartElement(false);
				}
				break;
			case START_ARRAY: 
				processStartElement(true);
				break;
			case KEY_NAME:
				processStartElement(parser.getString());
				break;
			case END_ARRAY: 
				processEndElement();
			case END_OBJECT:
				processEndElement();
				break;
			case VALUE_FALSE:
				processValueElement("false");
				break;
			case VALUE_TRUE:
				processValueElement("true");
				break;
			case VALUE_NULL:
				processValueElement(null);
				break;
			case VALUE_NUMBER:
			case VALUE_STRING:
				processValueElement(parser.getString());
				break;
			default: 
				logger.trace("processEvent; unknown event: {}", event);
		}			
	}
	
	private Data getTopData() {
		for (int i = dataStack.size() - 1; i >= 0; i--) {
			Data data = dataStack.elementAt(i);
			if (data != null && data.getElement() != null) {
				return data;
			}
		}
		return null;
	}

	private void processDocument(String name) throws XDMException {

		String root = "/" + (name == null ? "" : name);
		docType = model.translateDocumentType(root);
		Path path = model.translatePath(docType, "", NodeKind.document, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne);
		Element start = new Element();
		start.setElementId(elementId++);
		Data data = new Data(path, start);
		dataStack.add(data);
		dataList.add(data);
	}

	private boolean isAttribute(String name) {
		return name.startsWith("-") || name.startsWith("@");
	}
	
	private void processStartElement(boolean isArray) {
		if (isArray) {
			dataStack.add(null);
		} else {
			Data current = dataStack.lastElement();  
			if (current == null || current.getNodeKind() != NodeKind.element) {
				dataStack.add(null);
			}
		}
	}
	
	private void processStartElement(String name) throws XDMException {
		
		Data parent = getTopData();
		if (!name.equals(parent.getName())) {
			Data current = null;
			if (isAttribute(name)) {
				name = name.substring(1);
				if (name.startsWith("xmlns")) {
					current = addData(parent, NodeKind.namespace, "/#" + name, null, XQItemType.XQBASETYPE_STRING, Occurrence.zeroOrOne);
				} else {
					current = addData(parent, NodeKind.attribute, "/@" + name, null, XQItemType.XQBASETYPE_ANYATOMICTYPE, Occurrence.zeroOrOne);
				}
			} else if (name.equals("#text")) {
				current = new Data(null, null);  
			} else {
				current = addData(parent, NodeKind.element, "/" + name, null, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.zeroOrOne); 
			}
			if (current != null) {
				dataStack.add(current);
			}
		}
	}

	private void processEndElement() {
		if (dataStack.size() > 0) {
			Data current = dataStack.pop();
			logger.trace("processEndElement; got current: {}", current);
		}
	}

	private void processValueElement(String value) throws XDMException {
		
		Data current = dataStack.pop();
		boolean isArray = current == null;
		if (isArray || current.getElement() == null) {
			current = getTopData();
		}
		if (current.getNodeKind() == NodeKind.element) {
			addData(current, NodeKind.text, "/text()", value, XQItemType.XQBASETYPE_ANYATOMICTYPE, 
					isArray ? Occurrence.zeroOrMany : Occurrence.zeroOrOne);
		} else {
			current.getElement().setValue(value);
		}
		if (isArray) {
			dataStack.add(null);
		}
	}	
	
}
