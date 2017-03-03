package com.bagri.core.server.api.df.xml;

import static com.bagri.support.util.FileUtils.EOL;
import static com.bagri.support.util.FileUtils.def_encoding;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import com.bagri.core.DataKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.model.Element;
import com.bagri.core.model.Elements;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Path;
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
 		//
 		//logger.trace("init; got context: {}", context);
 	}
 
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String buildString(Map<DataKey, Elements> elements) throws BagriException {
    	StringBuffer buff = new StringBuffer();
    	Collection<Data> dataList = buildDataList(elements);
    	
    	Stack<Data> dataStack = new Stack<Data>();
    	boolean eltOpen = false;
    	
    	for (Data data: dataList) {
    		String name = data.getName();
    		switch (data.getNodeKind()) {
    			case document: { // this must be the first row..
    				buff.append("<?xml version=\"1.0\"?>").append(EOL); // what about: encoding="utf-8"?>
    				break;
    			}
    			case namespace: { 
    				buff.append(" ").append("xmlns");
    				if (name != null && name.trim().length() > 0) {
    					buff.append(":").append(name);
    				}
    				buff.append("=\"").append(data.getValue()).append("\"");
    				break;
    			}
    			case element: {
    				eltOpen = endElement(dataStack, data, buff, eltOpen);
    	    		// add idents..
    				dataStack.add(data);
       				buff.append("<").append(name); 
       				eltOpen = true;
       				break;
    			}
    			case attribute: { 
    				if (!dataStack.isEmpty()) {
    					buff.append(" ").append(name).append("=\"").append(data.getValue()).append("\"");
    				} else {
    					buff.append(data.getValue());
    				}
    				break;
    			}
    			case comment: { 
    				eltOpen = endElement(dataStack, data, buff, eltOpen);
    	    		// add idents..
    				buff.append("<!--").append(data.getValue()).append("-->"); 
    				if (dataStack.isEmpty()) {
    					buff.append(EOL);
    				}
    				break;
    			}
    			case pi: { 
    				eltOpen = endElement(dataStack, data, buff, eltOpen);
    	    		// add idents..
    				buff.append("<?").append(name).append(" ");
    				buff.append(data.getValue()).append("?>"); 
    				if (dataStack.isEmpty()) {
    					buff.append(EOL);
    				}
    				break;
    			}
    			case text: {
    				eltOpen = endElement(dataStack, data, buff, eltOpen);
    	    		// add idents..
    				buff.append(data.getValue());
    				break;
    			}
    			default: {
    				//logger.warn("buildXml; unknown NodeKind: {}", data.getNodeKind());
    			}
    		}
    	}
    	
		while (dataStack.size() > 0) {
			Data top = dataStack.pop();
			if (eltOpen) {
				buff.append("/>").append(EOL);
				eltOpen = false;
			} else {
				buff.append("</").append(top.getName()).append(">").append(EOL);
			}
		}
		// add idents..
    	
    	return buff.toString();
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream buildStream(Map<DataKey, Elements> elements) throws BagriException {
		String content = buildString(elements);
		if (content != null) {
			try {
				return new ByteArrayInputStream(content.getBytes(def_encoding));
			} catch (UnsupportedEncodingException ex) {
				throw new BagriException(ex, BagriException.ecInOut);
			}
		}
		return null;
	}
	
    private boolean endElement(Stack<Data> dataStack, Data data, StringBuffer buff, boolean eltOpen) {
    	
		if (dataStack.isEmpty()) {
			//
		} else {
			Data top = dataStack.peek();
			//if (data.getParentId() == top.getElementId()) {
				// new child element
			//	if (eltOpen) {
			//		buff.append(">");
			//		eltOpen = false;
			//	}
			//	if (data.getNodeKind() != NodeKind.text) {
			//		buff.append(EOL);
			//	}
			//} else {
			//	while (top != null && data.getParentId() != top.getElementId()) {
			//		if (eltOpen) {
			//			buff.append("/>").append(EOL);
			//			eltOpen = false;
			//		} else {
			//			buff.append("</").append(top.getName()).append(">").append(EOL);
			//		}
    		//		dataStack.pop();
    		//		if (dataStack.isEmpty()) {
    		//			top = null;
    		//		} else {
    		//			top = dataStack.peek();
    		//		}
			//	}
			//}
		}
		return eltOpen;
    }
    
    private Collection<Data> buildDataList(Map<DataKey, Elements> elements) {

    	List<Data> dataList = new ArrayList<>(elements.size() * 2);
    	// here the source elements contain elements with values only
    	// we should enrich the collection with intermediate parents
    	for (Map.Entry<DataKey, Elements> entry: elements.entrySet()) {
    		int pathId = entry.getKey().getPathId();
    		Path path = model.getPath(pathId);
    		if (path == null) {
        		logger.info("buildDataSet; can't get path for pathId: {}", pathId);
        		continue;
    		}
    		
    		Elements elts = entry.getValue();
    		for (Element element: elts.getElements()) {
    			Data data = new Data(path, element);
    			dataList.add(data);
    		}
    	}
    	Collections.sort(dataList);
    	return dataList;
    }

}
