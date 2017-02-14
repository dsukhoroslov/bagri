package com.bagri.core.server.api.df.json;

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

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.model.Element;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentParserBase;

/**
 * XDM Parser implementation for JSON data format. Uses reference implementation (Glassfish) of json streaming parser.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class JsonApiParser extends ContentParserBase implements ContentParser {
	
	// TODO: move all of this to the init method !?
	private static JsonParserFactory factory;
	static {
		//JsonProvider provider = JsonProvider.provider();
		//Map<String, Boolean> config = new HashMap<String, Boolean>();
		//config.put(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES.name(), true);
		//config.put(JsonParser.Feature.ALLOW_COMMENTS.name(), true);
		//config.put(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.name(), true);
		//provider.createParserFactory(config); // Understands JsonFactory and JsonParser features
		
		Map<String, Object> params = new HashMap<>();
		//params.put("javax.json.spi.JsonProvider", "com.github.pgelinas.jackson.javax.json.spi.JacksonProvider");
		factory = Json.createParserFactory(params);
	}
	
	/**
	 * 
	 * @param model the model management component
	 * @param json the document content in JSON format
	 * @return the list of parsed XDM data elements
	 * @throws IOException in case of content read exception
	 * @throws BagriException in case of content parse exception
	 */
	public static List<Data> parseDocument(ModelManagement model, String json) throws IOException, BagriException {
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
	public List<Data> parse(String json) throws BagriException { 
		try (Reader reader = new StringReader(json)) {
			return parse(reader);
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(File file) throws BagriException {
		try (Reader reader = new FileReader(file)) {
			return parse(reader);
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(InputStream stream) throws BagriException {

		try (JsonParser parser = factory.createParser(stream)) {
			return parse(parser);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Data> parse(Reader reader) throws BagriException {
		
		try (JsonParser parser = factory.createParser(reader)) {
			return parse(parser);
		}
	}

	/**
	 * 
	 * @param parser the JSON streaming parser
	 * @return the list of parsed XDM data elements
	 * @throws BagriException in case of any parsing error
	 */
	public List<Data> parse(JsonParser parser) throws BagriException {
		
		ParserContext ctx = init();
		while (parser.hasNext()) {
			processEvent(ctx, parser);
		}
		return ctx.getDataList();
	}
	
	private void processEvent(ParserContext ctx, JsonParser parser) throws BagriException { //, XMLStreamException {

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
				if (ctx.getStackSize() == 0) {
					parser.next();
					processDocument(ctx, parser.getString());
					processStartElement(ctx, parser.getString());
				} else {
					processStartElement(ctx, false);
				}
				break;
			case START_ARRAY: 
				processStartElement(ctx, true);
				break;
			case KEY_NAME:
				processStartElement(ctx, parser.getString());
				break;
			case END_ARRAY: 
				processEndElement(ctx);
			case END_OBJECT:
				processEndElement(ctx);
				break;
			case VALUE_FALSE:
				processValueElement(ctx, "false");
				break;
			case VALUE_TRUE:
				processValueElement(ctx, "true");
				break;
			case VALUE_NULL:
				processValueElement(ctx, null);
				break;
			case VALUE_NUMBER:
			case VALUE_STRING:
				processValueElement(ctx, parser.getString());
				break;
			default: 
				logger.trace("processEvent; unknown event: {}", event);
		}			
	}
	
	private Data getTopData(ParserContext ctx) {
		for (int i = ctx.getStackSize() - 1; i >= 0; i--) {
			Data data = ctx.getStackElement(i);
			if (data != null && data.getElement() != null) {
				return data;
			}
		}
		return null;
	}

	private void processDocument(ParserContext ctx, String name) throws BagriException {

		String root = "/" + (name == null ? "" : name);
		ctx.setDocType(model.translateDocumentType(root));
		Path path = model.translatePath(ctx.getDocType(), "", NodeKind.document, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne);
		Element start = new Element();
		start.setElementId(ctx.nextElementId());
		Data data = new Data(path, start);
		ctx.addStack(data);
		ctx.addData(data);
	}

	private boolean isAttribute(String name) {
		return name.startsWith("-") || name.startsWith("@");
	}
	
	private void processStartElement(ParserContext ctx, boolean isArray) {
		if (isArray) {
			ctx.addStack(null);
		} else {
			Data current = ctx.lastData();  
			if (current == null || current.getNodeKind() != NodeKind.element) {
				ctx.addStack(null);
			}
		}
	}
	
	private void processStartElement(ParserContext ctx, String name) throws BagriException {
		
		Data parent = getTopData(ctx);
		if (!name.equals(parent.getName())) {
			Data current = null;
			if (isAttribute(name)) {
				name = name.substring(1);
				if (name.startsWith("xmlns")) {
					current = addData(ctx, parent, NodeKind.namespace, "/#" + name, null, XQItemType.XQBASETYPE_STRING, Occurrence.zeroOrOne);
				} else {
					current = addData(ctx, parent, NodeKind.attribute, "/@" + name, null, XQItemType.XQBASETYPE_ANYATOMICTYPE, Occurrence.zeroOrOne);
				}
			} else if (name.equals("#text")) {
				current = new Data(null, null);  
			} else {
				current = addData(ctx, parent, NodeKind.element, "/" + name, null, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.zeroOrOne); 
			}
			if (current != null) {
				ctx.addStack(current);
			}
		}
	}

	private void processEndElement(ParserContext ctx) {
		if (ctx.getStackSize() > 0) {
			Data current = ctx.popData();
			logger.trace("processEndElement; got current: {}", current);
		}
	}

	private void processValueElement(ParserContext ctx, String value) throws BagriException {
		
		Data current = ctx.peekData();
		boolean isArray = current == null;
		if (isArray || current.getElement() == null) {
			current = getTopData(ctx);
		}
		if (current.getNodeKind() == NodeKind.element) {
			addData(ctx, current, NodeKind.text, "/text()", value, XQItemType.XQBASETYPE_ANYATOMICTYPE, 
					isArray ? Occurrence.zeroOrMany : Occurrence.zeroOrOne);
		} else {
			current.getElement().setValue(value);
		}
		if (isArray) {
			ctx.addStack(null);
		}
	}	
	
}