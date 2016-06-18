/**
 * 
 */
package com.bagri.xquery.saxon;

import static com.bagri.xdm.common.XDMConstants.bg_schema;

import java.io.StringReader;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.bagri.common.util.FileUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.cache.api.XDMDocumentManagement;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.SourceResolver;
import net.sf.saxon.trans.XPathException;

/**
 * @author Denis Sukhoroslov
 *
 */
public class SourceResolverImpl implements SourceResolver, URIResolver {
	
	private static final Logger logger = LoggerFactory.getLogger(SourceResolverImpl.class);
	
    private XDMRepository repo;

    public SourceResolverImpl(XDMRepository repo) {
    	this.repo = repo;
    }
	
	@Override
	public Source resolve(String href, String base) throws TransformerException {
		logger.trace("resolve. href: {}; base: {}", href, base);
		return resolveSource(new StreamSource(href), null);
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.lib.SourceResolver#resolveSource(javax.xml.transform.Source, net.sf.saxon.Configuration)
	 */
	@Override
	public Source resolveSource(Source source, Configuration config) throws XPathException {
		logger.trace("resolveSource. source: {}; config: {}", source.getSystemId(), config);
		
		String content = null;
		String original = source.getSystemId();

		URI uri = URI.create(original);
		logger.trace("resolveSource. got {} URI: {}", uri.isAbsolute() ? "absolute" : "relative", uri);
		
		try {
			if (bg_schema.equals(uri.getScheme())) {
				// skip leading "/"
				long docKey = Long.parseLong(uri.getPath().substring(1));
				content = ((XDMDocumentManagement) repo.getDocumentManagement()).getDocumentAsString(docKey);
			} else {
				String src = original;
				if ("file".equals(uri.getScheme())) {
					// here we search by fileName
					src = FileUtils.getPathName(src);
				}
				logger.debug("resolveSource; not a native schema {}, trying short uri: {}", uri.getScheme(), src); 
				content = repo.getDocumentManagement().getDocumentAsString(src);
				//if (ids.size() > 0) {
				//	docId = ids.iterator().next();
				//} else if ("file".equals(uri.getScheme())) {
					// here we search by full name
				//	src = FileUtils.path2Uri(src);
				//	logger.debug("resolveSource; got no results; trying full uri: {}", src); 
				//	ids = repo.getDocumentManagement().getDocumentIds(src);
				//	if (ids.size() > 0) {
				//		docId = ids.iterator().next();
				//	}
				//}
			}
		} catch (XDMException ex) {
			throw new XPathException("cannot resolve document for URI: " +  uri);
		}
		
		if (content == null) {
			throw new XPathException("cannot resolve document for URI: " +  uri);
		}

		//Source src = mgr.getDocumentAsSource(docId);
		//if (src != null) {
		//	logger.trace("resolveSource. got document from cache, returning: {}", src);
		//	//if (source instanceof TinyDocumentImpl) {
		//	((TinyDocumentImpl) src).getTree().setConfiguration(config);
		//	return src;
		//}
		
		// move this processing to a node (member)
		// the document belongs to
		//logger.debug("resolveSource; looking for documentId: {}", docId);
		//String content;
		//try {
			// another bottleneck! takes 6.73 ms, even to get XML from cache! !?
		//	content = repo.getDocumentManagement().getDocumentAsString(docId);
			//content = content.replaceAll("&", "&amp;");
		//} catch (XDMException ex) {
		//	throw new XPathException(ex);
		//}
		 
		if (content != null && content.trim().length() > 0) {
			logger.trace("resolveSource; got content: {}", content.length());
			StreamSource ss = new StreamSource(new StringReader(content));
			ss.setSystemId(original);
		    
			//InputSource is = new InputSource(new StringReader(content));
		    //is.setSystemId(original);
		    //Source ss = new SAXSource(is);
		    //ss.setSystemId(original);
			
			// bottleneck! takes 15 ms. Cache DocumentInfo in Saxon instead! 
			//NodeInfo doc = config.buildDocument(ss);
			//mgr.storeDocumentSource(docId, doc);
			//return doc;

			//mgr.storeDocumentSource(docId, ss);
			return ss;
		}
		logger.trace("resolveSource. got empty content: '{}'", content);
		return null;
	}
	
}
