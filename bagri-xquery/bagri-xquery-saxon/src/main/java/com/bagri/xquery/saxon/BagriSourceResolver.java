/**
 * 
 */
package com.bagri.xquery.saxon;

import java.io.StringReader;
import java.net.URI;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.util.FileUtils;
import com.bagri.xdm.access.api.XDMDocumentManagement;

import static com.bagri.xqj.BagriXQConstants.bg_schema;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.PJConverter;
import net.sf.saxon.lib.ExternalObjectModel;
import net.sf.saxon.lib.SourceResolver;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyDocumentImpl;

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
		
		Long docId;
		String original = source.getSystemId();

		// TODO: use config.getSystemURIResolver() !
		
		URI uri = URI.create(original);
		logger.trace("resolveSource. got {} URI: {}", uri.isAbsolute() ? "absolute" : "relative", uri);
		if (bg_schema.equals(uri.getScheme())) {
			// skip leading "/"
			docId = Long.parseLong(uri.getPath().substring(1));
		} else {
			String src = original;
			if ("file".equals(uri.getScheme())) { 
				src = FileUtils.path2Uri(src);
			}
			logger.debug("resolveSource; not a native schema {}, trying uri: {}", uri.getScheme(), src); 
			docId = mgr.getDocumentId(src);
		}

		//Source src = null; //((XDMDocumentManagementServer) mgr).getDocumentSource(docId);
		//if (src != null) {
		//	logger.trace("resolveSource. got document from cache, returning: {}", src);
		//	//if (source instanceof TinyDocumentImpl) {
		//	((TinyDocumentImpl) src).getTree().setConfiguration(config);
		//	return src;
		//}
		
		// can return just a custom source (containing docId) from here,
		// but perform the real resolution to the NodeInfo in unravel method
		
		// move this processing to a node (member)
		// the document belongs to
		logger.debug("resolveSource; looking for documentId: {}", docId);
		// another bottleneck! takes 6.73 ms, even to get XML from cache! !?
		String content = mgr.getDocumentAsString(docId);
		//content = content.replaceAll("&", "&amp;");
		 
		if (content != null && content.trim().length() > 0) {
			logger.trace("resolveSource; got content: {}", content.length());
			StreamSource ss = new StreamSource(new StringReader(content));
			// bottleneck! takes 15 ms. Cache DocumentInfo in Saxon instead! 
			NodeInfo doc = config.buildDocument(ss);
			//((XDMDocumentManagementServer) mgr).putDocumentSource(docId, doc);
			return doc;
		}
		logger.trace("resolveSource. got empty content: '{}'", content);
		return null;
	}


	@Override
	public Receiver getDocumentBuilder(Result result) throws XPathException {
		logger.trace("getDocumentBuilder. result: {}", result);
		return null;
	}


	@Override
	public String getIdentifyingURI() {
		logger.trace("getIdentifyingURI"); 
		return null;
	}

	private JPConverter jpc = null;

	@Override
	public JPConverter getJPConverter(Class sourceClass, Configuration config) {
		if (jpc == null) { 
			jpc = new BagriJPConverter();
			logger.trace("getJPConverter. source: {}; new JPC instance: {}", sourceClass, jpc);
		}
		return jpc;
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
		// invoked after every document resolution:
		// unravel. source: net.sf.saxon.tree.tiny.TinyDocumentImpl@153; config: net.sf.saxon.Configuration@ce339b2
		// thus, can move document resolution from resolve to this method
		// but, what for ?
		//logger.trace("unravel. source: {}; config: {}", source, config);
		if (source instanceof TinyDocumentImpl) {
			TinyDocumentImpl doc = (TinyDocumentImpl) source;
			doc.getTree().setConfiguration(config);
			return doc;
		} 
		logger.info("unravel. source: {}; config: {}", source, config);
		return null;
	}

}
