/**
 * 
 */
package com.bagri.xquery.saxon;

import java.io.StringReader;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xquery.XQItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.util.FileUtils;
import com.bagri.xdm.access.api.XDMDocumentManagement;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.PJConverter;
import net.sf.saxon.lib.ExternalObjectModel;
import net.sf.saxon.lib.SourceResolver;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pull.PullSource;
import net.sf.saxon.trans.XPathException;

/**
 * @author Denis Sukhoroslov
 *
 */
public class BagriSourceResolver implements SourceResolver, ExternalObjectModel {
	
    /**
	 * need it because ExternalObjectModel extends Serializable
	 */
	private static final long serialVersionUID = 766503700601897539L;

	private static final Logger logger = LoggerFactory.getLogger(BagriSourceResolver.class);
	
    private XDMDocumentManagement mgr;

    public BagriSourceResolver(XDMDocumentManagement mgr) {
    	this.mgr = mgr;
    }
	
	/* (non-Javadoc)
	 * @see net.sf.saxon.lib.SourceResolver#resolveSource(javax.xml.transform.Source, net.sf.saxon.Configuration)
	 */
	@Override
	public Source resolveSource(Source source, Configuration config) throws XPathException {
		logger.trace("resolveSource. source: {}; config: {}", source.getSystemId(), config);
		//String[] path = source.getSystemId().split("/");
		//int docId = Integer.parseInt(path[path.length - 1]);
		//String content = mgr.getDocumentAsString(docId);
		String uri = FileUtils.path2Uri(source.getSystemId());
		String content = mgr.getDocumentAsString(uri);
		
		//content = content.replaceAll("&", "&amp;");
		
		if (content != null && content.trim().length() > 0) {
			StreamSource src = new StreamSource(new StringReader(content));
			DocumentInfo doc = config.buildDocument(src);
			return doc;
		}
		logger.trace("resolveSource. got empty content: '{}'", content);
		return null;
		
		//Source src = new BagriXDMSource(config);
		//Source src = new PullSource(new BagriXDMPullProvider(config));
		//Source src = new com.saxonica.pull.UnconstructedDocument();
		//src.setSystemId(source.getSystemId());
		//return src;
	}


	@Override
	public Receiver getDocumentBuilder(Result result) throws XPathException {
		logger.trace("getDocumentBuilder. result: {}", result);
		return null;
	}


	@Override
	public String getIdentifyingURI() {
		logger.trace("getIdentifyingURI"); //, targetClass);
		return null;
	}


	@Override
	public JPConverter getJPConverter(Class sourceClass, Configuration config) {
		//if (sourceClass.isAssignableFrom(XQItem.class)) {
			logger.trace("getJPConverter. source: {}; returning custom converter", sourceClass);
			return new BagriJPConverter();
		//}
		//logger.trace("getJPConverter. source: {}; returning null", sourceClass);
		//return null;
	}


	@Override
	public PJConverter getNodeListCreator(Object node) {
		logger.trace("getNodeListCreator. node: {}", node);
		return null;
	}


	@Override
	public PJConverter getPJConverter(Class targetClass) {
		logger.trace("getPJConverter. target: {}", targetClass);
		return null;
	}


	@Override
	public boolean sendSource(Source source, Receiver receiver) throws XPathException {
		logger.trace("sendSource. source: {}; receiver: {}", source, receiver);
		return true;
	}


	@Override
	public NodeInfo unravel(Source source, Configuration config) {
		logger.trace("unravel. source: {}; config: {}", source, config);
		if (source instanceof BagriXDMSource) {
			return (BagriXDMSource) source;
		}
		return null;
	}

}
