package com.bagri.xdm.access.coherence.impl;

import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_ELEMENT;
import static com.bagri.xdm.access.api.XDMCacheConstants.SQN_DOCUMENT;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.Source;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathExpression;
import com.bagri.xdm.access.api.XDMDocumentManagementClient;
//import com.bagri.xdm.cache.data.PathParentDocumentKey;
//import com.bagri.xdm.cache.data.PathValueDocumentKey;
import com.bagri.xdm.access.coherence.process.DocumentBuilder;
import com.bagri.xdm.access.coherence.process.DocumentCreator;
import com.bagri.xdm.access.coherence.process.DocumentRemover;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMNodeKind;
import com.oracle.coherence.common.sequencegenerators.ClusteredSequenceGenerator;
import com.oracle.coherence.common.sequencegenerators.SequenceGenerator;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.GreaterEqualsFilter;
import com.tangosol.util.filter.GreaterFilter;
import com.tangosol.util.filter.LessEqualsFilter;
import com.tangosol.util.filter.LessFilter;

public class DocumentManagementClient extends XDMDocumentManagementClient { 
	
	private NamedCache xddCache;
	private NamedCache xdmCache;
	private SequenceGenerator dGen;
	// must be configurable...
	private int batchSize = 100;
	
	public DocumentManagementClient() {
		super();
		//initializeHazelcast();
		mFactory = new CoherenceXDMFactory();
		//CacheFactory.getConfigurableCacheFactory()
		
		mDictionary = new CoherenceSchemaDictionary(CacheFactory.getConfigurableCacheFactory());
		initializeServices();
	}
	
	@Override
	public void close() {
		CacheFactory.shutdown();
	}
	
	private void initializeServices() {
		xddCache = CacheFactory.getCache(CN_XDM_DOCUMENT);
		xdmCache = CacheFactory.getCache(CN_XDM_ELEMENT);
		//xpvCache = getCache(CN_XDM_PATH_VALUE_INDEX);
		dGen = new ClusteredSequenceGenerator(SQN_DOCUMENT, 100);
		logger.trace("initialize; got cache: {}", xdmCache);
	}
	
	NamedCache getDataCache() {
		return xdmCache;
	}
	
	NamedCache getDocumentCache() {
		return xddCache;
	}

	@Override
	public Long getDocumentId(String uri) {
		Filter f = new EqualsFilter(new PofExtractor(String.class, 1), uri);
		Set<Long> docKeys = new HashSet<Long>(xddCache.keySet(f));
		if (docKeys.size() == 0) {
			return null;
		}
		// todo: check if too many docs ??
		return docKeys.iterator().next();
	}
	
	@Override
	public XDMDocument getDocument(long docId) {
		return (XDMDocument) xddCache.get(docId);
	}

	@Override
	public XDMDocument storeDocument(String xml) {

		long docId = dGen.next();
		String uri = "/library/" + docId; 
		return storeDocument(docId, uri, xml);
	}

	@Override
	public XDMDocument storeDocument(long docId, String uri, String xml) {
		// todo: override existing document -> create a new version ?
		logger.trace("storeDocument.enter; xml: {}; uri: {}, docId: {}", xml.length(), uri, docId);
		DocumentCreator proc = new DocumentCreator(uri, xml);
		Object result = xddCache.invoke(docId, proc);
		logger.trace("storeDocument.exit; returning: {}", result);
		return (XDMDocument) result;
	}

	//@Override
	public void removeDocument(long docId) {
		
		logger.trace("removeDocument.enter; docId: {}", docId);
		DocumentRemover proc = new DocumentRemover();
		Object result = xddCache.invoke(docId, proc);
		logger.trace("removeDocument.exit; removed: {}", result);
	}
	
	@Override
	public Collection<String> getXML(ExpressionContainer query, String template, Map params) {
		long stamp = System.currentTimeMillis();
		
		Collection<String> uris = getDocumentURIs(query);
		if (uris.size() > 0) {
			DocumentBuilder ta = new DocumentBuilder(query.getExpression().getRoot().getDocType(), template, params);
			Object result = xddCache.aggregate(uris, ta);
			logger.trace("getXml.exit; got aggregation results: {}; time taken {}", result, System.currentTimeMillis() - stamp);
			return (Collection<String>) result;
		}
		return Collections.emptyList();
	}
	
	//@Override
	public String getDocumentAsString(long docId) {
		
		long stamp = System.currentTimeMillis();
		XDMDocument xdoc = (XDMDocument) xddCache.get(docId);
		int docType = xdoc.getTypeId();
		String path = mDictionary.getDocumentRoot(docType);

		Map<String, String> params = new HashMap<String, String>();
		params.put(":doc", path);
		DocumentBuilder ta = new DocumentBuilder(docType, ":doc", params);
		Object result = xddCache.aggregate(Collections.singletonList(docId), ta);
		logger.trace("getDocumentAsString.exit; got aggregation results: {}; time taken {}", result, System.currentTimeMillis() - stamp);
		
		Collection<String> c = (Collection<String>) result;
		if (c.isEmpty()) {
			return null;
		}
		return c.iterator().next();
	}
	
	@SuppressWarnings("rawtypes")
	private Filter getValueFilter(PathExpression pex, Object value) {
		//Object value = pex.getValue();
		ValueExtractor ve;
		if (value instanceof Integer) {
			ve = new ReflectionExtractor("asInt"); 
		} else if (value instanceof Long) {
			ve = new ReflectionExtractor("asLong");
		} else if (value instanceof Boolean) {
			ve = new ReflectionExtractor("asBoolean");
		} else if (value instanceof Byte) {
			ve = new ReflectionExtractor("asByte");
		} else if (value instanceof Short) {
			ve = new ReflectionExtractor("asShort");
		} else if (value instanceof Float) {
			ve = new ReflectionExtractor("asFloat");
		} else if (value instanceof Double) {
			ve = new ReflectionExtractor("asDouble");
		} else {
			value = value.toString();
			ve = new PofExtractor(String.class, 6);
		}
	
		switch (pex.getCompType()) {
			case EQ: return new EqualsFilter(ve, value);
			case LE: return new LessEqualsFilter(ve, (Comparable) value);
			case LT: return new LessFilter(ve, (Comparable) value);
			case GE: return new GreaterEqualsFilter(ve, (Comparable) value);
			case GT: return new GreaterFilter(ve, (Comparable) value);
			default: return null;
		}
	}
	
	//@Override
	protected Set<Long> queryPathKeys(Set<Long> found, PathExpression pex, Object value) {

		int pathId = -1;
		if (pex.isRegex()) {
			Set<Integer> pathIds = mDictionary.translatePathFromRegex(pex.getDocType(), pex.getRegex());
			logger.trace("queryPathKeys; regex: {}; pathIds: {}", pex.getRegex(), pathIds);
			if (pathIds.size() > 0) {
				pathId = pathIds.iterator().next();
			}
		} else {
			String path = pex.getFullPath();
			logger.trace("queryPathKeys; path: {}; comparison: {}", path, pex.getCompType());
			pathId = mDictionary.translatePath(pex.getDocType(), path, XDMNodeKind.fromPath(path)).getPathId();
		}
		//String val = value.toString();
		Filter valueFilter = getValueFilter(pex, value);
		if (valueFilter == null) {
			throw new IllegalArgumentException("Can't construct filter for expression: " + pex);
		}

		Filter f = new AndFilter(
				new EqualsFilter(new PofExtractor(Integer.class, 4), pathId), valueFilter);
		Set<XDMDataKey> keys = xdmCache.keySet(f);
		logger.trace("queryPathKeys; path: {}, value: {}; got keys: {}; cache size: {}", 
				pathId, value, keys.size(), xdmCache.size()); 
		
		if (keys.size() > 0) {
			Set<Long> docIds = new HashSet<Long>();
			for (XDMDataKey key: keys) {
				docIds.add(key.getDocumentId());
			}
			found.retainAll(docIds);
		} else {
			found.clear();
		}
		return found;
	}
	
	@Override
	public Collection<Long> getDocumentIDs(ExpressionContainer query) {
		//Filter f = new EqualsFilter(new PofExtractor(Integer.class, 2), query.getRoot().getDocType());
		//Set<Long> keys = new HashSet<Long>(xddCache.keySet(f));
		//return queryKeys(keys, query.getRoot());
		
		// moved to the server side..
		return null;
	}

	@Override
	public Collection<String> getDocumentURIs(ExpressionContainer query) {
		// moved to the server side..
		return null;
	}

	@Override
	public Object executeXCommand(String command, Map bindings, Properties props) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object executeXQuery(String query, Map bindings, Properties props) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Source getDocumentAsSource(long arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XDMDocument storeDocumentSource(long arg0, Source arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
