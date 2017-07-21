/**
 * 
 */
package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.bg_schema;

import java.io.Reader;
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

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.BagriException;
import com.bagri.core.server.api.DocumentManagement;
import com.bagri.support.util.FileUtils;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.SourceResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.trans.XPathException;

/**
 * @author Denis Sukhoroslov
 *
 */
public class SourceResolverImpl implements SourceResolver, URIResolver, UnparsedTextURIResolver {
	
	private static final Logger logger = LoggerFactory.getLogger(SourceResolverImpl.class);
	
    private SchemaRepository repo;

    public SourceResolverImpl(SchemaRepository repo) {
    	this.repo = repo;
    }
	
    /**
     * {@inheritDoc}
     */
	@Override
	public Source resolve(String href, String base) throws TransformerException {
		logger.trace("resolve. href: {}; base: {}", href, base);
		return resolveSource(new StreamSource(href), null);
	}

	/**
	 * {@inheritDoc} 
	 */
	@Override
	public Source resolveSource(Source source, Configuration config) throws XPathException {
		logger.trace("resolveSource. source: {}; config: {}", source.getSystemId(), config);
		
		String original = source.getSystemId();

		URI uri = URI.create(original);
		logger.trace("resolveSource. got {} URI: {}", uri.isAbsolute() ? "absolute" : "relative", uri);

		String content = resolveContent(uri); 

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

	@Override
	public Reader resolve(URI absoluteURI, String encoding, Configuration config) throws XPathException {
		logger.trace("resolve; uri: {}; encoding: {}", absoluteURI, encoding);
		String content = resolveContent(absoluteURI); 
		return new StringReader(content);
	}
	
	private Object resolveUri(URI uri) {
		Object result;
		if (bg_schema.equals(uri.getScheme())) {
			// skip leading "/"
			result = new Long(uri.getPath().substring(1));
		} else {
			String src = uri.toString();
			if ("file".equals(uri.getScheme())) {
				// here we search by fileName
				src = FileUtils.getPathName(src);
			}
			result = src;
		}
		logger.debug("resolveUri; uri schema: {}, returning: {}", uri.getScheme(), result); 
		return result;
	}
	
	private String resolveContent(URI uri) throws XPathException {

		String content;
		try {
			Object key = resolveUri(uri);
			if (key instanceof Long) {
				content = ((DocumentManagement) repo.getDocumentManagement()).getDocumentAsString((Long) key, null);
			} else {
				content = repo.getDocumentManagement().getDocumentAsString((String) key, null);
			}
			
			// we want to get MAP here, not a String! need access to other parameters in context..

			if (content == null) {
				throw new XPathException("cannot resolve document for URI: " +  uri); 
			}
			return content;
		} catch (BagriException ex) {
			throw new XPathException("cannot resolve document for URI: " +  uri, ex);
		}
	}
	
}
