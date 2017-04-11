package com.bagri.samples.ext.df;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JaksonParser extends ContentParserBase implements ContentParser {
	
	private static JsonFactory factory = new JsonFactory();

	public static List<Data> parseDocument(ModelManagement model, String json) throws BagriException {
		JaksonParser parser = new JaksonParser(model);
		parser.init(new Properties());
		return parser.parse(json);
	}
	
	public JaksonParser(ModelManagement model) {
		super(model);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(Properties properties) {
		for (Map.Entry prop: properties.entrySet()) {
			String name = (String) prop.getKey();
			JsonParser.Feature f = JsonParser.Feature.valueOf(name);
			if (f != null) {
				factory.configure(f, Boolean.valueOf((String) prop.getValue()));
			}
		}
	}
	
	private void closeParser(JsonParser jParser) {
		if (jParser != null) {
			try {
				jParser.close();
			} catch (IOException ex) {
				logger.error("closeParser.error", ex);
			}
		}
	}
	
	@Override
	public List<Data> parse(String json) throws BagriException { 

		JsonParser jParser = null;
		try {
			jParser = factory.createParser(json);	
			return parse(jParser);
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		} finally {
			closeParser(jParser);
		}
	}
	
	@Override
	public List<Data> parse(File file) throws BagriException {

		JsonParser jParser = null;
		try {
			jParser = factory.createParser(file);	
			return parse(jParser);
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		} finally {
			closeParser(jParser);
		}
	}
	
	@Override
	public List<Data> parse(InputStream stream) throws BagriException {
		
		JsonParser jParser = null;
		try {
			jParser = factory.createParser(stream);	
			return parse(jParser);
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		} finally {
			closeParser(jParser);
		}
	}
	
	@Override
	public List<Data> parse(Reader reader) throws BagriException {
		
		JsonParser jParser = null;
		try {
			jParser = factory.createParser(reader);	
			return parse(jParser);
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		} finally {
			closeParser(jParser);
		}
	}

	public List<Data> parse(JsonParser parser) throws BagriException {
		
		logger.trace("parse.enter; context: {}; schema: {}", parser.getParsingContext(), parser.getSchema());
		ParserContext ctx = initContext();
		try {
			while (parser.nextToken() != null) {
				processToken(ctx, parser);
			}
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		}
		return ctx.getDataList();
	}
	
	private void processToken(ParserContext ctx, JsonParser parser) throws IOException, BagriException { //, XMLStreamException {

		JsonToken token = parser.getCurrentToken();
		logger.trace("processToken; got token: {}; name: {}; value: {}", token.name(), parser.getCurrentName(), parser.getText());
		
		switch (token) {
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
			case NOT_AVAILABLE:
				break;
			case FIELD_NAME:
				ctx.addData(parser.getCurrentName());
				break;
			case END_OBJECT:
			case END_ARRAY: 
				ctx.endElement();
				break;
			case VALUE_EMBEDDED_OBJECT:
				// ???
				break;
			case VALUE_FALSE:
				ctx.addValue(false);
				break;
			case VALUE_NULL:
				ctx.addValue();
				break;
			case VALUE_NUMBER_FLOAT:
				ctx.addValue(parser.getDecimalValue());
				break;
			case VALUE_NUMBER_INT:
				ctx.addValue(parser.getLongValue());
				break;
			case VALUE_TRUE:
				ctx.addValue(true);
				break;
			case VALUE_STRING:
				ctx.addValue(parser.getText());
				break;
			default: 
				logger.trace("processToken; unknown token: {}", token);
		}			
	}

/*	
	private void processDocument(ParserContext ctx, String name) throws BagriException {

		String root = "/" + (name == null ? "" : name);
		ctx.setDocType(model.translateDocumentType(root));
		Path path = model.translatePath(ctx.getDocType(), "", NodeKind.document, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne); 
		Element start = new Element();
		Data data = new Data(path, start);
		ctx.addStack(data);
		ctx.addData(data);
	}
	
	private boolean isAttribute(String name) {
		return name.startsWith("-") || name.startsWith("@");
	}

	private void processStartElement(ParserContext ctx, String name) throws BagriException {
		
		if (name != null && !isAttribute(name)) {
			Data parent = ctx.peekData();
			if (name.equals("#text")) {
				// add marker
				ctx.addStack(null);
			} else if (!name.equals(parent.getName())) {
				Data current = addData(ctx, parent, NodeKind.element, "/" + name, null, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.zeroOrOne); 
				ctx.addStack(current);
			}
		}
	}

	private void processEndElement(ParserContext ctx) {

		ctx.popData();
	}

	private void processValueElement(ParserContext ctx, String name, String value) throws BagriException {
		
		//value = value.replaceAll("&", "&amp;");
		if (name == null) {
			Data current = ctx.peekData();
			if (current == null) {
				// #text in array; not sure it'll always work.
				// use XDMJsonParser.getTopData instead ?
				current = ctx.getStackElement(ctx.getStackSize() - 2);
				// this is for Deque
				//Iterator<Data> itr = dataStack.descendingIterator();
				//itr.next();
				//current = itr.next();
			}
			addData(ctx, current, NodeKind.text, "/text()", value, XQItemType.XQBASETYPE_ANYATOMICTYPE, Occurrence.zeroOrOne);
		} else if (isAttribute(name)) {
			Data current = ctx.peekData(); 
			name = name.substring(1);
			if (name.startsWith("xmlns")) {
				addData(ctx, current, NodeKind.namespace, "/#" + name, value, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne);
			} else {
				addData(ctx, current, NodeKind.attribute, "/@" + name, value, XQItemType.XQBASETYPE_ANYATOMICTYPE, Occurrence.zeroOrOne);
			}
		} else {
			Data current = ctx.popData();
			if (current == null) {
				// #text
				current = ctx.peekData(); 
			}
			addData(ctx, current, NodeKind.text, "/text()", value, XQItemType.XQBASETYPE_ANYATOMICTYPE, Occurrence.zeroOrOne);
		}
	}
*/	
}
