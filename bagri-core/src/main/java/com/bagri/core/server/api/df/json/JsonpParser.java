package com.bagri.core.server.api.df.json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParserFactory;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.ParseResults;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentParserBase;

/**
 * XDM Parser implementation for JSON data format. Uses reference implementation (Glassfish) of json streaming parser.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class JsonpParser extends ContentParserBase implements ContentParser<String> {
	
	private JsonParserFactory factory = Json.createParserFactory(null);
	
	/**
	 * 
	 * @param model the model management component
	 * @param json the document content in JSON format
	 * @return the list of parsed XDM data elements
	 * @throws IOException in case of content read exception
	 * @throws BagriException in case of content parse exception
	 */
	public static ParseResults parseDocument(ModelManagement model, String json) throws IOException, BagriException {
		JsonpParser parser = new JsonpParser(model);
		return parser.parse(json);
	}
	
	/**
	 * 
	 * @param model the model management component
	 */
	JsonpParser(ModelManagement model) {
		super(model);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public void init(Properties properties) {
		// process/convert any specific properties here 
		Map<String, Object> params = new HashMap<>();
		for (Map.Entry prop: properties.entrySet()) {
			//String name = (String) prop.getKey();
			params.put((String) prop.getKey(), prop.getValue());
		}
		factory = Json.createParserFactory(params);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ParseResults parse(String json) throws BagriException { 
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
	public ParseResults parse(File file) throws BagriException {
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
	public ParseResults parse(InputStream stream) throws BagriException {
		try (JsonParser parser = factory.createParser(stream)) {
			return parse(parser);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ParseResults parse(Reader reader) throws BagriException {
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
	public ParseResults parse(JsonParser parser) throws BagriException {
		
		ParserContext ctx = initContext();
		while (parser.hasNext()) {
			processEvent(ctx, parser);
		}
		return ctx.getParseResults();
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
				if (ctx.getTopData() == null) {
					ctx.addDocument("/");
				} else {
					ctx.addElement();
				}
				break;
			case START_ARRAY: 
				ctx.addArray();
				break;
			case KEY_NAME:
				ctx.addData(parser.getString());
				break;
			case END_ARRAY: 
			case END_OBJECT:
				ctx.endElement();
				break;
			case VALUE_FALSE:
				ctx.addValue(false);
				break;
			case VALUE_TRUE:
				ctx.addValue(true);
				break;
			case VALUE_NULL:
				ctx.addValue();
				break;
			case VALUE_NUMBER:
				if (parser.isIntegralNumber()) {
					ctx.addValue(parser.getLong());
				} else {
					ctx.addValue(parser.getBigDecimal());
				}
				break;
			case VALUE_STRING:
				ctx.addValue(parser.getString());
				break;
			default: 
				logger.trace("processEvent; unknown event: {}", event);
		}			
	}
	
}

