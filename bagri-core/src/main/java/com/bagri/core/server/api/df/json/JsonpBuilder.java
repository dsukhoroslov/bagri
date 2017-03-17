package com.bagri.core.server.api.df.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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
 *  NOTE: not implemented yet!
 * 
 * @author Denis Sukhoroslov
 *
 */
public class JsonpBuilder extends ContentBuilderBase implements ContentBuilder {
	
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
	public String buildString(Collection<Data> elements) throws BagriException {
		Writer writer = new StringWriter();
    	JsonGenerator stream = factory.createGenerator(writer);
		
    	boolean eltOpen = false;
		Deque<Data> dataStack = new LinkedList<>();
    	for (Data data: elements) {
    		eltOpen = writeElement(dataStack, stream, data, eltOpen);
    	}

    	stream.flush();
    	String result = writer.toString();
    	try {
			writer.close();
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		}
    	return result;  
	}

	private boolean writeElement(Deque<Data> dataStack, JsonGenerator stream, Data data, boolean eltOpen) {
		switch (data.getNodeKind()) {
			case document: { // this must be the first row..
				//buff.append("<?xml version=\"1.0\"?>"); // what about: encoding="utf-8"?>
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
				//eltOpen = endElement(dataStack, buff, data, eltOpen, prefix);
				//dataStack.push(data);
   				//buff.append("<").append(data.getName()); 
   				//eltOpen = true;
   				break;
			}
			case attribute: {
				// check data type..
				stream.write(data.getName(), data.getValue().toString());
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
				//eltOpen = endElement(dataStack, buff, data, eltOpen, prefix);
				//buff.append(data.getValue());
				break;
			}
			default: {
				//logger.warn("buildXml; unknown NodeKind: {}", data.getNodeKind());
			}
		}
		return eltOpen;
	}
	
	private boolean endElement(Deque<Data> dataStack, StringBuffer buff, Data data, boolean eltOpen, String prefix) {
    	
		if (dataStack.isEmpty()) {
			//
		} else {
			Data top = dataStack.peek();
			if (data.getLevel() == top.getLevel() + 1 && data.getParentPos() == top.getPos()) {
				// new child element
				if (eltOpen) {
					buff.append(">");
					eltOpen = false;
				}
			} else {
				while (top != null && (data.getParentPos() != top.getPos() || data.getLevel() != top.getLevel() + 1)) {
					if (eltOpen) {
						buff.append("/>");
						eltOpen = false;
					} else {
						buff.append("</").append(top.getName()).append(">");
					}
    				dataStack.pop();
   					top = dataStack.peek();
				}
			}
		}
		return eltOpen;
    }
	
	
}
