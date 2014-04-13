package com.bagri.xdm.access.xml;

import java.util.Collection;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.XDMElement;
import com.bagri.xdm.XDMPath;
import com.bagri.xdm.access.api.XDMSchemaDictionary;

public class XmlBuilder {

	private static final Logger logger = LoggerFactory.getLogger(XmlBuilder.class);
	
    public static String buildXml(XDMSchemaDictionary dict, Collection<XDMElement> xdms) {
    	StringBuffer buff = new StringBuffer();
    	Stack<XDMElement> dataStack = new Stack<XDMElement>();
    	boolean eltOpen = false;
    	for (XDMElement xdm: xdms) {
    		//logger.trace("buildXml; xdm: {}", xdm);
    		XDMPath path = dict.getPath(xdm.getPathId());
    		if (path ==  null) {
    			logger.warn("Can't find path for element: {}", xdm);
    		}
    		
    		switch (xdm.getKind()) {
    			case document: { // this must be the first row..
    				//dataStack.add(xdm);
    				break;
    			}
    			case namespace: { 
    				buff.append(" ").append("xmlns");
    				if (xdm.getName() != null && xdm.getName().trim().length() > 0) {
    					buff.append(":").append(xdm.getName());
    				}
   					buff.append("=\"").append(xdm.getValue()).append("\"");
    				break;
    			}
    			case element: {
    				if (dataStack.isEmpty()) {
    					//
    				} else {
	    				XDMElement top = dataStack.peek();
	    				if (xdm.getParentId() == top.getElementId()) {
	    					// new child element
	    					if (eltOpen) {
	    						buff.append(">");
	    						eltOpen = false;
	    					}
	    					buff.append("\n");
	    				} else {
	    					while (top != null && xdm.getParentId() != top.getElementId()) {
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
    	    		// add idents..
    				dataStack.add(xdm);
       				buff.append("<").append(xdm.getName()); 
       				eltOpen = true;
       				break;
    			}
    			case attribute: { 
    				if (!dataStack.isEmpty()) {
    					buff.append(" ").append(xdm.getName()).append("=\"").append(xdm.getValue()).append("\"");
    				} else {
    					buff.append(xdm.getValue());
    				}
    				break;
    			}
    			case comment: { 
    				if (eltOpen) {
    					buff.append(">");
    					eltOpen = false;
    				}
    				buff.append("\n").append("<!--").append(xdm.getValue()).append("-->");
    				break;
    			}
    			case pi:  { // not processed yet..
    				break;
    			}
    			case text:  { 
    				if (dataStack.isEmpty()) {
    					//
    				} else {
	    				XDMElement top = dataStack.peek();
	    				if (xdm.getParentId() == top.getElementId()) {
	    					// new child element
	    					if (eltOpen) {
	    						buff.append(">");
	    						eltOpen = false;
	    					}
	    				} else {
	    					while (top != null && xdm.getParentId() != top.getElementId()) {
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
    	    		// add idents..
    				buff.append(xdm.getValue());
    				break;
    			}
    			default: {
    				//logger.warn("buildXml; unknown NodeKind: {}", xdm.getNodeKind());
    			}
    		}
    	}
    	
		while (dataStack.size() > 0) {
			XDMElement top = dataStack.pop();
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
	
}
