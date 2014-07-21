package com.bagri.xdm.access.api;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.PathExpression;
import com.bagri.xdm.access.xml.XDMStaxParser;
import com.bagri.xdm.access.xml.XmlBuilder;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;

public abstract class XDMDocumentManagerServer extends XDMDocumentManagerBase /*implements XDMDocumentManager*/ {

    protected IdGenerator<Long> idGen;
    
    public void setElementIdGenerator(IdGenerator<Long> idGen) {
    	this.idGen = idGen;
    }
	
    public abstract Collection<String> buildDocument(int docType, String template, Map<String, String> params, Set entries);
	public abstract XDMDocument createDocument(Entry<String, XDMDocument> entry, long docId, String xml);
	public abstract void deleteDocument(Entry<String, XDMDocument> entry);
	public abstract XDMDocument updateDocument(Entry<String, XDMDocument> entry, boolean newVersion, String xml); // + audit info?
	
	protected List<XDMElement> parseDocument(String xml, long id) {

		List<XDMElement> elements;
		Reader reader = new StringReader(xml);
		XDMStaxParser parser = new XDMStaxParser(mDictionary, id, idGen);
		try {
			elements = parser.parse(reader);
		} catch (IOException | XMLStreamException ex) {
			logger.error("parseDocument; can't parse document", ex);
			return null;
		} finally {
			try {
				reader.close();
			} catch (IOException ex) {
				logger.error("parseDocument; can't close reader", ex);
			}
		}
		logger.trace("parseDocument; after parse got {} elements", elements.size());
		return elements;
	}
	
	protected XDMElement getDocumentRoot(List<XDMElement> elements) {

		for (Iterator<XDMElement> itr = elements.iterator(); itr.hasNext();) {
			XDMElement xdm = itr.next();
			if (xdm.getKind() == XDMNodeKind.element) {
				return xdm;
			}
		}
		return null;
	}
	
    protected String buildXml(Set<Map.Entry> xdEntries) {
    	
        logger.trace("buildXml.enter; got entries: {}", xdEntries.size()); 
        
        List<XDMElement> xdmList = new ArrayList<XDMElement>(xdEntries.size()); 
        for (Map.Entry xdEntry : xdEntries) {
        	XDMElement xdm = (XDMElement) xdEntry.getValue();
        	xdmList.add(xdm);
        }
        
	long stamp = System.currentTimeMillis();
        logger.trace("buildXml; before sort; list size: {}", xdmList.size()); 
        Collections.sort(xdmList, new XDMElementComparator());
	stamp = System.currentTimeMillis() - stamp;
        logger.trace("buildXml; after sort; time taken: {}", stamp); 
	stamp = System.currentTimeMillis();
	String xml = XmlBuilder.buildXml(mDictionary, xdmList);
	stamp = System.currentTimeMillis() - stamp;
        logger.trace("buildXml.exit; returning xml length: {}; time taken: {}", xml.length(), stamp); 
	return xml;
    }
    
	@Override
	protected Set<Long> queryPathKeys(Set<Long> found, PathExpression pex) {
		return null;
	}	
	
	//@Override
	//public Collection<Long> getDocumentIDs(ExpressionBuilder query) {
	//	return null;
	//}
	
	//protected abstract Long getDocumentId(String uri);
	
	//@Override
	//public XDMDocument getDocument(String uri) {
	//	return getDocument(getDocumentId(uri));
	//}

	//@Override
	//public String getDocumentAsString(String uri) {
	//	return getDocumentAsString(getDocumentId(uri));
	//}
	
	//@Override
	//public void removeDocument(String uri) {
	//	removeDocument(getDocumentId(uri));
	//}
}
