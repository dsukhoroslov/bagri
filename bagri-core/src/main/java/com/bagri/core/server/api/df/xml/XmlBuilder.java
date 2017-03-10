package com.bagri.core.server.api.df.xml;

import static com.bagri.core.Constants.pn_schema_builder_pretty;
import static com.bagri.core.Constants.pn_schema_builder_ident;
import static com.bagri.support.util.FileUtils.EOL;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Properties;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.model.NodeKind;
import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentBuilderBase;

/**
 * XDM Builder implementation for XML format. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XmlBuilder extends ContentBuilderBase implements ContentBuilder {
	
	private boolean pretty = false;
	private int ident = 4;

	/**
	 * 
	 * @param model the XDM model management component
	 */
	public XmlBuilder(ModelManagement model) {
		super(model);
	}
	
 	/**
  	 * {@inheritDoc}
  	 */
 	public void init(Properties properties) {
 		logger.trace("init; got properties: {}", properties);
 		String value = properties.getProperty(pn_schema_builder_pretty, "false");
 		pretty = Boolean.valueOf(value);
 		value = properties.getProperty(pn_schema_builder_ident, "4");
 		ident = Integer.parseInt(value);
 	}
 
	/**
	 * {@inheritDoc}
	 */
	@Override
   	public String buildString(Collection<Data> elements) throws BagriException {
    	
    	Deque<Data> dataStack = new LinkedList<>();
    	StringBuffer buff = new StringBuffer();
    	boolean eltOpen = false;

    	String prefix = "";
    	for (int i=0; i < ident; i++) {
    		prefix += " ";
    	}
    	
    	for (Data data: elements) {
    		eltOpen = writeElement(dataStack, buff, data, eltOpen, prefix);
    	}

    	boolean writeIdent = false;
		while (dataStack.size() > 0) {
			Data top = dataStack.pop();
			if (eltOpen) {
				buff.append("/>");
				eltOpen = false;
			} else {
				if (writeIdent) {
					addIdents(top, buff, prefix);
				}
				buff.append("</").append(top.getName()).append(">");
			}
			if (pretty) {
				buff.append(EOL);
				writeIdent = true;
			}
		}
    	return buff.toString();
    }

	private boolean writeElement(Deque<Data> dataStack, StringBuffer buff, Data data, boolean eltOpen, String prefix) {
		switch (data.getNodeKind()) {
			case document: { // this must be the first row..
				buff.append("<?xml version=\"1.0\"?>"); // what about: encoding="utf-8"?>
				if (pretty) {
					buff.append(EOL);
				}
				break;
			}
			case namespace: { 
				buff.append(" ").append("xmlns");
				String name = data.getName();
				if (name != null && name.trim().length() > 0) {
					buff.append(":").append(name);
				}
				buff.append("=\"").append(data.getValue()).append("\"");
				break;
			}
			case element: {
				eltOpen = endElement(dataStack, buff, data, eltOpen, prefix);
				dataStack.push(data);
				// don't add idents if we're in mixed content (text)!
				addIdents(data, buff, prefix);
   				buff.append("<").append(data.getName()); 
   				eltOpen = true;
   				break;
			}
			case attribute: { 
				if (!dataStack.isEmpty()) {
					buff.append(" ").append(data.getName()).append("=\"").append(data.getValue()).append("\"");
				} else {
					buff.append(data.getValue());
				}
				break;
			}
			case comment: { 
				eltOpen = endElement(dataStack, buff, data, eltOpen, prefix);
				addIdents(data, buff, prefix);
				buff.append("<!--").append(data.getValue()).append("-->"); 
				if (dataStack.isEmpty() && pretty) {
					buff.append(EOL);
				}
				break;
			}
			case pi: { 
				eltOpen = endElement(dataStack, buff, data, eltOpen, prefix);
				addIdents(data, buff, prefix);
				buff.append("<?").append(data.getName()).append(" ");
				buff.append(data.getValue()).append("?>"); 
				if (dataStack.isEmpty() && pretty) {
					buff.append(EOL);
				}
				break;
			}
			case text: {
				eltOpen = endElement(dataStack, buff, data, eltOpen, prefix);
				buff.append(data.getValue());
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
				if (data.getNodeKind() != NodeKind.text && pretty) {
					// don't add EOL if we're in mixed content (text)!
					buff.append(EOL);
				}
			} else {
				boolean writeIdent = false;
				while (top != null && (data.getParentPos() != top.getPos() || data.getLevel() != top.getLevel() + 1)) {
					if (eltOpen) {
						buff.append("/>");
						eltOpen = false;
					} else {
						if (writeIdent) {
							addIdents(top, buff, prefix);
						}
						buff.append("</").append(top.getName()).append(">");
					}
					if (pretty) {
						buff.append(EOL);
    					writeIdent = true;
					}
    				dataStack.pop();
   					top = dataStack.peek();
				}
			}
		}
		return eltOpen;
    }
	
	private void addIdents(Data data, StringBuffer buff, String space) {
		if (pretty) {
			for (int i=1; i < data.getLevel(); i++) {
				buff.append(space);
			}
		}
	}
    
}
