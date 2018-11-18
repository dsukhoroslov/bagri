/**
 * 
 */
package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.bg_schema;
import static com.bagri.core.Constants.pn_document_content;
import static com.bagri.core.Constants.pn_document_headers;
import static com.bagri.support.util.FileUtils.def_encoding;

import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
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
	
	private Object content;
	private Configuration config;
    private SchemaRepository repo;

    public SourceResolverImpl(SchemaRepository repo, Configuration config) {
    	this.repo = repo;
    	this.config = config;
    }
    
    public void setDocumentContent(Object content) {
    	this.content = content;
    }
	
    /**
     * {@inheritDoc}
     */
	@Override
	public Source resolve(String href, String base) throws TransformerException {
		logger.trace("resolve. href: {}; base: {}", href, base);
		return resolveSource(new StreamSource(href), config);
	}

	/**
	 * {@inheritDoc} 
	 */
	@Override
	public Source resolveSource(Source source, Configuration conf) throws XPathException {
		logger.trace("resolveSource. source: {}; config: {}", source.getSystemId(), conf);
		
		URI uri;
		String original = source.getSystemId();
		try {
			String encoded = URLEncoder.encode(original, def_encoding); 
			uri = URI.create(encoded);
			logger.trace("resolveSource; got encoded {} URI: {}", encoded, uri);
		} catch (UnsupportedEncodingException ex) {
			throw new XPathException(ex);
		}
		
		String content = resolveContent(uri); 
		if (content != null && content.trim().length() > 0) {
			logger.trace("resolveSource; got content: {}", content.length());
			StreamSource ss = new StreamSource(new StringReader(content));
			// original or encoded??
			ss.setSystemId(original);
			return ss;
		}
		logger.trace("resolveSource. got empty content: '{}'", content);
		return null;
	}

	@Override
	public Reader resolve(URI absoluteURI, String encoding, Configuration conf) throws XPathException {
		logger.trace("resolve; uri: {}; encoding: {}", absoluteURI, encoding);
		String content = resolveContent(absoluteURI); 
		return new StringReader(content);
	}
	
	private Object resolveUri(URI uri) throws UnsupportedEncodingException {
		Object result;
		if (bg_schema.equals(uri.getScheme())) {
			// skip leading "/"
			result = new Long(uri.getPath().substring(1));
		} else {
			String src = URLDecoder.decode(uri.toString(), def_encoding);
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

   		if (content != null) {
   			return content.toString();
   		}

		DocumentAccessor doc;
		try {
			Object key = resolveUri(uri);
			Properties props = new Properties();
			props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CONTENT));
			if (key instanceof Long) {
				doc = ((DocumentManagement) repo.getDocumentManagement()).getDocument((Long) key, props);
			} else {
				doc = repo.getDocumentManagement().getDocument((String) key, props);
			}
			
			// we want to get MAP here, not a String! need access to other parameters in context..
			//config.getParseOptions()

			if (doc == null) {
				throw new XPathException("cannot resolve document for URI: " +  uri); 
			}
			return doc.getContent();
		} catch (UnsupportedEncodingException ex) {
			throw new XPathException("cannot resolve URI: " +  uri, ex);
		} catch (BagriException ex) {
			throw new XPathException("cannot resolve document for URI: " +  uri, ex);
		}
	}
	
}
