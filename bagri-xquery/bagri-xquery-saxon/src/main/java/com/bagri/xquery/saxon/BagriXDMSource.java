package com.bagri.xquery.saxon;

import java.util.Iterator;

import javax.xml.transform.Source;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyAxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BagriXDMSource implements DocumentInfo { //Source {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriXDMSource.class);
	
    private String systemId = "XDM";
    private Configuration config;
    
    public BagriXDMSource(Configuration config) {
    	this.config = config;
    }

	@Override
	public String getSystemId() {
		logger.debug("getSystemId. returning {}", systemId);
		return systemId;
	}

	@Override
	public void setSystemId(String systemId) {
		logger.debug("setSystemId. received {}", systemId);
		this.systemId = systemId;
	}

	@Override
	public AtomicSequence atomize() throws XPathException {
		// TODO Auto-generated method stub
		logger.debug("atomize. returning {}", "null");
		return null;
	}

	@Override
	public int compareOrder(NodeInfo node) {
		// TODO Auto-generated method stub
		logger.debug("compareOrder. nodeInfo: {}; returning {}", node, 0);
		return 0;
	}

	@Override
	public int comparePosition(NodeInfo node) {
		// TODO Auto-generated method stub
		logger.debug("comparePosition. nodeInfo: {}; returning {}", node, 0);
		return 0;
	}

	@Override
	public void copy(Receiver out, int copyOpts, int locationId) throws XPathException {
		// TODO Auto-generated method stub
		logger.debug("copy. Receiver: {}; copyOptions: {}; locationId: {}", 
				new Object[] {out, copyOpts, locationId});
	}

	@Override
	public void generateId(FastStringBuffer buffer) {
		// TODO Auto-generated method stub
		logger.debug("generateId. buffer {}", buffer);
	}

	@Override
	public String getAttributeValue(String uri, String local) {
		// TODO Auto-generated method stub
		logger.debug("getAttributeValue. URI: {}; local: {}; returning {}", 
				new Object[] {uri, local, "null"});
		return null;
	}

	@Override
	public String getBaseURI() {
		// TODO Auto-generated method stub
		logger.debug("getBaseURI. returning {}", "null");
		return null;
	}

	@Override
	public int getColumnNumber() {
		// TODO Auto-generated method stub
		logger.debug("getColumnNumber. returning {}", 0);
		return 0;
	}

	@Override
	public Configuration getConfiguration() {
		
		return config;
	}

	@Override
	public NamespaceBinding[] getDeclaredNamespaces(NamespaceBinding[] bindings) {
		//String query = "declare namespace s=\;\n" +
		logger.debug("getDeclaredNamespaces. bindings: {}", bindings);
		NamespaceBinding[] result = new NamespaceBinding[1];
		result[0] = new NamespaceBinding("s", "http://tpox-benchmark.com/security");
		return result;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		logger.debug("getDisplayName. returning {}", "null");
		return null;
	}

	@Override
	public long getDocumentNumber() {
		// TODO Auto-generated method stub
		logger.debug("getDocumentNumber. returning {}", 0);
		return 0;
	}

	@Override
	public DocumentInfo getDocumentRoot() {
		logger.debug("getDocumentRoot. returning {}", this);
		return this; //null;
	}

	@Override
	public int getFingerprint() {
		// TODO Auto-generated method stub
		logger.debug("getFingerpring. returning {}", 0);
		return 0;
	}

	@Override
	public int getLineNumber() {
		// TODO Auto-generated method stub
		logger.debug("getLineNumber. returning {}", 0);
		return 0;
	}

	@Override
	public String getLocalPart() {
		// TODO Auto-generated method stub
		logger.debug("getLocalPart. returning {}", "null");
		return null;
	}

	@Override
	public int getNameCode() {
		// TODO Auto-generated method stub
		logger.debug("getNameCode. returning {}", 0);
		return 0;
	}

	@Override
	public NamePool getNamePool() {
		// TODO Auto-generated method stub
		logger.debug("getNamePool. returning {}", "null");
		return null;
	}

	@Override
	public int getNodeKind() {
		// TODO Auto-generated method stub
		logger.debug("getNodeKind. returning {}", 0);
		return 0;
	}

	@Override
	public NodeInfo getParent() {
		// TODO Auto-generated method stub
		logger.debug("getParent. returning {}", "null");
		return null;
	}

	@Override
	public String getPrefix() {
		// TODO Auto-generated method stub
		logger.debug("getPrefix. returning {}", "null");
		return null;
	}

	@Override
	public NodeInfo getRoot() {
		// TODO Auto-generated method stub
		logger.debug("getRoot. returning {}", "null");
		return null;
	}

	@Override
	public SchemaType getSchemaType() {
		
		logger.debug("getSchemaTYpe. returning {}", "untyped");
		return Untyped.getInstance();
	}

	@Override
	public String getStringValue() {
		// TODO Auto-generated method stub
		logger.debug("getStringValue. returning {}", "null");
		return null;
	}

	@Override
	public int getTypeAnnotation() {
		// TODO Auto-generated method stub
		logger.debug("getTypeAnnotation. returning {}", 0);
		return 0;
	}

	@Override
	public String getURI() {
		// TODO Auto-generated method stub
		logger.debug("getURI. returning {}", "null");
		return null;
	}

	@Override
	public boolean hasChildNodes() {
		// TODO Auto-generated method stub
		logger.debug("hasChildNodes. returning {}", false);
		return false;
	}

	@Override
	public boolean isId() {
		// TODO Auto-generated method stub
		logger.debug("isId. returning {}", false);
		return false;
	}

	@Override
	public boolean isIdref() {
		// TODO Auto-generated method stub
		logger.debug("isIdRef. returning {}", false);
		return false;
	}

	@Override
	public boolean isNilled() {
		// TODO Auto-generated method stub
		logger.debug("isNilled. returning {}", false);
		return false;
	}

	@Override
	public boolean isSameNodeInfo(NodeInfo node) {
		// TODO Auto-generated method stub
		logger.debug("isSameNodeInfo. NodeInfo: {}; returning {}", node, false);
		return false;
	}

	@Override
	public AxisIterator iterateAxis(byte axisNumber) {
		// TODO Auto-generated method stub
		logger.debug("iterateAxis. number: {}; returning {}", axisNumber, "null");
		return null;
	}

	@Override
	public AxisIterator iterateAxis(byte axisNumber, NodeTest test) {
		
		logger.debug("iterateAxis. axis number: {}; test: {}; returning {}", 
				new Object[] {axisNumber, test, "empty"});
		if (test instanceof NameTest) {
			logger.debug("iterateAxis. test URI: {}; test LocalPart: {}", 
					((NameTest) test).getNamespaceURI(), ((NameTest) test).getLocalPart());
		}
		return EmptyAxisIterator.emptyAxisIterator();
	}

	@Override
	public CharSequence getStringValueCS() {
		// TODO Auto-generated method stub
		logger.debug("getStringValueCS. returning {}", "null");
		return null;
	}

	@Override
	public Item head() throws XPathException {
		// TODO Auto-generated method stub
		logger.debug("head. returning {}", "null");
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceIterator<? extends Item> iterate() throws XPathException {
		logger.debug("iterate. returning {}", "empty");
		return EmptyIterator.getInstance();
	}

	@Override
	public String[] getUnparsedEntity(String name) {
		// TODO Auto-generated method stub
		logger.debug("getUnparsedEntity. name: {}; returning {}", name, "null");
		return null;
	}

	@Override
	public Iterator<String> getUnparsedEntityNames() {
		// TODO Auto-generated method stub
		logger.debug("getUnparsedEntityNames. returning {}", "null");
		return null;
	}

	@Override
	public Object getUserData(String key) {
		// TODO Auto-generated method stub
		logger.debug("getUserData. key: {}; returning {}", key, "null");
		return null;
	}

	@Override
	public boolean isTyped() {
		// TODO Auto-generated method stub
		logger.debug("isTyped. returning {}", "false");
		return false;
	}

	@Override
	public NodeInfo selectID(String id, boolean getParent) {
		// TODO Auto-generated method stub
		logger.debug("selectId. id: {}; getParent: {}; returning {}", 
				new Object[] {id, getParent, "null"});
		return null;
	}

	@Override
	public void setUserData(String key, Object value) {
		// TODO Auto-generated method stub
		logger.debug("setUserData. key: {}; value {}", key, value);
	}

}
