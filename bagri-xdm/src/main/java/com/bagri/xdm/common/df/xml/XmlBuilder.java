package com.bagri.xdm.common.df.xml;

import static com.bagri.common.util.FileUtils.def_encoding;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.cache.api.XDMBuilder;
import com.bagri.xdm.common.DataKey;
import com.bagri.xdm.domain.Data;
import com.bagri.xdm.domain.Element;
import com.bagri.xdm.domain.Elements;
import com.bagri.xdm.domain.NodeKind;
import com.bagri.xdm.domain.Path;

/**
 * XDM Builder implementation for XML format. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XmlBuilder implements XDMBuilder {

	private static final Logger logger = LoggerFactory.getLogger(XmlBuilder.class);
	
	private XDMModelManagement model;
	
	/**
	 * 
	 * @param model the XDM model management component
	 */
	public XmlBuilder(XDMModelManagement model) {
		this.model = model;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String buildString(Map<DataKey, Elements> elements) throws XDMException {
    	StringBuffer buff = new StringBuffer();
    	Collection<Data> dataList = buildDataList(elements);
    	
    	Stack<Data> dataStack = new Stack<Data>();
    	boolean eltOpen = false;
    	//int idx = 0;
    	
    	for (Data data: dataList) {
    		//idx++;
    		//if (idx % 10000 == 0) {
    		//	logger.trace("buildXml; idx: {}; length: {}", idx, buff.length());
    		//}
    		
    		String name = data.getName();
    		switch (data.getNodeKind()) {
    			case document: { // this must be the first row..
    				buff.append("<?xml version=\"1.0\"?>").append("\n"); // what about: encoding="utf-8"?>
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
    					buff.append("\n");
    				}
    				break;
    			}
    			case pi:  { 
    				eltOpen = endElement(dataStack, data, buff, eltOpen);
    	    		// add idents..
    				buff.append("<?").append(name).append(" ");
    				buff.append(data.getValue()).append("?>"); 
    				if (dataStack.isEmpty()) {
    					buff.append("\n");
    				}
    				break;
    			}
    			case text:  {
    				eltOpen = endElement(dataStack, data, buff, eltOpen);
    	    		// add idents..
    				buff.append(data.getValue());
    				break;
    			}
    			default: {
    				//logger.warn("buildXml; unknown NodeKind: {}", xdm.getNodeKind());
    			}
    		}
    	}
    	
		while (dataStack.size() > 0) {
			Data top = dataStack.pop();
			if (eltOpen) {
				buff.append("/>").append("\n");
				eltOpen = false;
			} else {
				buff.append("</").append(top.getName()).append(">").append("\n");
			}
		}
		// add idents..
    	
    	return buff.toString();
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream buildStream(Map<DataKey, Elements> elements) throws XDMException {
		String content = buildString(elements);
		if (content != null) {
			try {
				return new ByteArrayInputStream(content.getBytes(def_encoding));
			} catch (UnsupportedEncodingException ex) {
				throw new XDMException(ex, XDMException.ecInOut);
			}
		}
		return null;
	}
	
    private boolean endElement(Stack<Data> dataStack, Data data, StringBuffer buff, boolean eltOpen) {
    	
		if (dataStack.isEmpty()) {
			//
		} else {
			Data top = dataStack.peek();
			if (data.getParentId() == top.getElementId()) {
				// new child element
				if (eltOpen) {
					buff.append(">");
					eltOpen = false;
				}
				if (data.getNodeKind() != NodeKind.text) {
					buff.append("\n");
				}
			} else {
				while (top != null && data.getParentId() != top.getElementId()) {
					if (eltOpen) {
						buff.append("/>").append("\n");
						eltOpen = false;
					} else {
						buff.append("</").append(top.getName()).append(">").append("\n");
					}
    				dataStack.pop();
    				if (dataStack.isEmpty()) {
    					top = null;
    				} else {
    					top = dataStack.peek();
    				}
				}
			}
		}
		return eltOpen;
    }
    
    private Collection<Data> buildDataList(Map<DataKey, Elements> elements) {

    	List<Data> dataList = new ArrayList<Data>(elements.size() * 2);
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
