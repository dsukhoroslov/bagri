package com.bagri.xquery.saxon;

import static com.bagri.xdm.common.XDMConstants.bg_schema;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.cache.api.DocumentManagement;
import com.bagri.xdm.cache.api.QueryManagement;
import com.bagri.xdm.query.ExpressionContainer;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.trans.XPathException;

public class ResourceCollectionImpl implements ResourceCollection {
	
    private static final Logger logger = LoggerFactory.getLogger(ResourceCollectionImpl.class);
	
	private String uri;
    private SchemaRepository repo;
	private ExpressionContainer query;
	private Collection<Long> docIds = null;
	private Iterator<Long> iter = null;
	
	
	public ResourceCollectionImpl(String uri, Collection<Long> docIds) {
		this.uri = uri;
		this.docIds = new ArrayList<>(docIds);
		this.iter = docIds.iterator();
	}

	public ResourceCollectionImpl(String uri, SchemaRepository repo, ExpressionContainer query) {
		this.uri = uri;
		this.repo = repo;
		this.query = query;
	}
	
	private void loadData() { //throws XPathException {
		try {
			docIds = ((QueryManagement) repo.getQueryManagement()).getDocumentIds(query);
		} catch (XDMException ex) {
			logger.error("loadData.error;", ex);
			//throw new XPathException(ex);
			docIds = Collections.emptyList();
		}
		logger.trace("loadData; got {} document ids", docIds.size());
		this.iter = docIds.iterator();
	}
	
	private boolean hasNext() {
		if (docIds == null) {
			loadData();
		}
		boolean result = iter.hasNext();
		logger.trace("hasNext; returning: {}", result);
		return result;
	}
	
	private Long next() { //throws XPathException {
		logger.trace("next.enter");
		if (docIds == null) {
			loadData();
		}
		Long currentId = iter.next();
		logger.trace("next.exit; returning: {}", currentId);
		return currentId;
	}
	
	@Override
	public String getCollectionURI() {
		return uri;
	}

	@Override
	public Iterator<String> getResourceURIs(XPathContext context) throws XPathException {
		logger.trace("getResourceURIs.enter;");
		return new UriIterator();
	}

	@Override
	public Iterator<? extends Resource> getResources(XPathContext context) throws XPathException {
		logger.trace("getResources.enter;");
		return new ResourceIterator();
	}

	@Override
	public boolean isStable(XPathContext context) {
		return true;
	}

	@Override
	public String toString() {
		return "ResourceCollectionImpl [query=" + query
				+ ", docIds=" + docIds /*+ ", current=" + current + ", position=" + position*/ + "]";
	}
	
	public class UriIterator implements Iterator<String> {

		@Override
		public boolean hasNext() {
			return ResourceCollectionImpl.this.hasNext();
		}

		@Override
		public String next() {
			Long next = ResourceCollectionImpl.this.next();
			if (next != null) {
				return bg_schema + ":/" + next;
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("method remove is not supported");
		}
		
	}

	public class ResourceIterator implements Iterator<Resource> {

		@Override
		public boolean hasNext() {
			return ResourceCollectionImpl.this.hasNext();
		}

		@Override
		public Resource next() {
			Long docKey = ResourceCollectionImpl.this.next();
			if (docKey != null) {
				String content;
				try {
					// another bottleneck! takes 6.73 ms, even to get XML from cache! !?
					content = ((DocumentManagement) repo.getDocumentManagement()).getDocumentAsString(docKey);
					//content = content.replaceAll("&", "&amp;");
				} catch (XDMException ex) {
					logger.error("next.error", ex);
					return null;
				}
				 
				if (content != null && !content.isEmpty()) {
					logger.trace("next; got content: {}", content.length());
					StreamSource ss = new StreamSource(new StringReader(content));
					ss.setSystemId(bg_schema + ":/" + docKey);
					// bottleneck! takes 15 ms. Cache DocumentInfo in Saxon instead! 
					//NodeInfo doc = config.buildDocument(ss);
					//mgr.storeDocumentSource(docId, doc);
					//return doc;

					//mgr.storeDocumentSource(docId, ss);
					return new ResourceImpl(ss);
				}
				logger.trace("resolveSource. got empty content'");
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("method remove is not supported");
		}
		
	}
}
