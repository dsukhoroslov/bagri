package com.bagri.core.server.api.df.json;

import static javax.xml.xquery.XQItemType.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.model.NodeKind;
import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentBuilderBase;

/**
 * Content Builder implementation for JSON format. Uses reference implementation (Glassfish) of json generator.
 * 
 *   
 * @author Denis Sukhoroslov
 *
 */
public class JsonpBuilder extends ContentBuilderBase<String> implements ContentBuilder<String> {
	
	private JsonGeneratorFactory factory = Json.createGeneratorFactory(null);
	
	/**
	 * 
	 * @param model the XDM model management component
	 */
	public JsonpBuilder(ModelManagement model) {
		super(model);
	}
	
 	/**
  	 * {@inheritDoc}
  	 */
 	public void init(Properties properties) {
 		logger.trace("init; got properties: {}", properties);
 		if (properties != null) {
 			Map<String, Object> params = new HashMap<>(properties.size());
 			for (Map.Entry e: properties.entrySet()) {
 				params.put(e.getKey().toString(), e.getValue());
 			}
 			factory = Json.createGeneratorFactory(params);
 		}
 	}
 
	@Override
	public String buildContent(Collection<Data> elements) throws BagriException {
		Writer writer = new StringWriter();
    	JsonGenerator stream = factory.createGenerator(writer);
		
		Deque<Data> dataStack = new LinkedList<>();
    	for (Data data: elements) {
    		writeElement(dataStack, stream, data);
    	}
    	
    	while (!dataStack.isEmpty()) {
    		stream.writeEnd();
    		dataStack.pop();
    	}

    	stream.flush();
    	String result = writer.toString();
    	try {
			writer.close();
		} catch (IOException ex) {
			// just skip it..
			logger.info("buildString; exception closing stream: {}", ex.getMessage());
		}
    	return result;  
	}

	private void writeElement(Deque<Data> dataStack, JsonGenerator stream, Data data) {
		switch (data.getNodeKind()) {
			case document: { // this must be the first row..
				stream.writeStartObject();
				dataStack.push(data);
				break;
			}
			case namespace: {
				String ns = "xmlns";
				String name = data.getName();
				if (name != null && name.trim().length() > 0) {
					ns += ":" + name;
				}
				stream.write(ns, data.getValue().toString());
				break;
			}
			case element: {
				endElement(dataStack, stream, data);
				// must call writeStartObject() in array!
				Data top = dataStack.peek();
				if (top != null && top.getNodeKind() == NodeKind.array) {
					stream.writeStartObject();
				} else {
					stream.writeStartObject(data.getName());
				}
				dataStack.push(data);
   				break;
			}
			case array: {
				//..
				endElement(dataStack, stream, data);
				stream.writeStartArray(data.getName());
				dataStack.push(data);
				break;
			}
			case attribute: {
				if (data.isNull()) {
					stream.writeNull(data.getName());
				} else {
					switch (data.getDataPath().getDataType()) {
						case XQBASETYPE_BOOLEAN: 
							stream.write(data.getName(), (Boolean) data.getValue());
							break;
						case XQBASETYPE_DECIMAL:
							stream.write(data.getName(), (BigDecimal) data.getValue());
							break;
						case XQBASETYPE_LONG:
							stream.write(data.getName(), (Long) data.getValue());
							break;
						default:
							stream.write(data.getName(), (String) data.getValue());
					}
				}
				break;
			}
			case comment: {
				// don't have comments in JSON ?
				break;
			}
			case pi: { 
				// don't have processing instructions in JSON ?
				break;
			}
			case text: {
				endElement(dataStack, stream, data);
				if (data.isNull()) {
					stream.writeNull();
				} else {
					switch (data.getDataPath().getDataType()) {
						case XQBASETYPE_BOOLEAN: 
							stream.write((Boolean) data.getValue());
							break;
						case XQBASETYPE_DECIMAL:
							stream.write((BigDecimal) data.getValue());
							break;
						case XQBASETYPE_LONG:
							stream.write((Long) data.getValue());
							break;
						default:
							stream.write((String) data.getValue());
					}
				}
				break;
			}
			default: {
				//logger.warn("writeElement; unknown NodeKind: {}", data.getNodeKind());
			}
		}
	}
	
	private void endElement(Deque<Data> dataStack, JsonGenerator stream, Data data) {
    	
		//while (top != null && (data.getParentPos() != top.getPos() || data.getLevel() != top.getLevel() + 1)) {
		do {
			Data top = dataStack.peek();
			if (top != null && (/*top.getLevel() > data.getLevel() ||*/ top.getPos() != data.getParentPos())) {
				stream.writeEnd();
				dataStack.pop();
			} else {
				break;
			}
		} while (true);
    }
	
	
}
