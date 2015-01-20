package com.bagri.xdm.cache.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.Source;

import com.bagri.common.query.BinaryExpression;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.Expression;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathExpression;
import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xdm.access.api.XDMDocumentManagementBase;
import com.bagri.xdm.access.xml.XmlBuilder;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMNodeKind;

public abstract class XDMDocumentManagementServer extends XDMDocumentManagementBase implements XDMDocumentManagement {

    public abstract Collection<String> buildDocument(Set<Long> docIds, String template, Map<String, String> params);
	public abstract XDMDocument createDocument(Entry<Long, XDMDocument> entry, String uri, String xml);
	public abstract void deleteDocument(Entry<Long, XDMDocument> entry);
	public abstract XDMDocument updateDocument(Entry<Long, XDMDocument> entry, boolean newVersion, String xml); // + audit info?
	
	public abstract Source getDocumentSource(long docId);
	public abstract void putDocumentSource(long docId, Source source);

	//public abstract ExpressionBuilder getQuery(String query, Map bindings);
	protected abstract Set<Long> queryPathKeys(Set<Long> found, PathExpression pex, Object value);
	//public abstract Collection<Long> getDocumentIDs(ExpressionBuilder query);
	
	protected XDMData getDataRoot(List<XDMData> elements) {

		for (XDMData data: elements) {
			if (data.getNodeKind() == XDMNodeKind.element) {
				return data;
			}
		}
		return null;
	}
	
    //protected String buildXml(Set<Map.Entry> xdEntries) {
    protected String buildXml(Map<XDMDataKey, XDMElements> xdmMap) {
    	
        logger.trace("buildXml.enter; got entries: {}", xdmMap.size()); 
        
        long stamp = System.currentTimeMillis();
        logger.trace("buildXml; before sort; list size: {}", xdmMap.size()); 
        //Collections.sort(xdmList, new XDMElementComparator());
        stamp = System.currentTimeMillis() - stamp;
        logger.trace("buildXml; after sort; time taken: {}", stamp); 
        stamp = System.currentTimeMillis();
        String xml = XmlBuilder.buildXml(mDictionary, xdmMap);
        stamp = System.currentTimeMillis() - stamp;
        logger.trace("buildXml.exit; returning xml length: {}; time taken: {}", xml.length(), stamp); 
        return xml;
    }
    
	public Set<Long> queryKeys(Set<Long> found, ExpressionContainer ec, Expression ex) {
		if (ex instanceof BinaryExpression) {
			BinaryExpression be = (BinaryExpression) ex;
			Set<Long> leftKeys = queryKeys(found, ec, be.getLeft());
			if (Comparison.AND.equals(be.getCompType())) {
				if (leftKeys.isEmpty()) {
					return leftKeys;
				}
				Set<Long> rightKeys = queryKeys(leftKeys, ec, be.getRight());
				return rightKeys;
			} else if (Comparison.OR.equals(be.getCompType())) {
				Set<Long> rightKeys = queryKeys(found, ec, be.getRight());
				leftKeys.addAll(rightKeys);
				return leftKeys;
			} else {
				throw new IllegalArgumentException("Wrong BinaryExpression type: " + be.getCompType());
			}
		}
		
		PathExpression pex = (PathExpression) ex;
		return queryPathKeys(found, pex, ec.getParam(pex));
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
