package com.bagri.xdm.process.hazelcast;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.xquery.XQException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.bagri.common.idgen.IdGenerator;
import com.bagri.common.manage.JMXUtils;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathExpression;

import static com.bagri.xdm.access.api.XDMCacheConstants.*;

import com.bagri.xdm.access.api.XDMQueryManagement;
import com.bagri.xdm.access.api.XDMSchemaDictionaryBase;
import com.bagri.xdm.access.hazelcast.impl.HazelcastXQCursor;
import com.bagri.xdm.access.hazelcast.process.XMLProvider;
import com.bagri.xdm.access.xml.XDMStaxParser;
import com.bagri.xdm.cache.common.XDMDocumentManagementServer;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMIndexKey;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElements;
import com.bagri.xdm.domain.XDMIndexedValue;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.api.XQProcessor;
import com.bagri.xquery.saxon.BagriExceptionIterator;
import com.bagri.xquery.saxon.BagriXQProcessor;
import com.hazelcast.core.Client;
import com.hazelcast.core.ClientListener;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.mapreduce.aggregation.Aggregation;
import com.hazelcast.mapreduce.aggregation.Aggregations;
import com.hazelcast.mapreduce.aggregation.Supplier;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentManagementServer extends XDMDocumentManagementServer implements ClientListener, ApplicationContextAware {
	
    private ApplicationContext appContext;
    private HazelcastInstance hzInstance;
    private XDMQueryManagement queryManager;

    private IdGenerator<Long> docGen;
	private IMap<Long, XDMDocument> xddCache;
    private IMap<Long, String> xmlCache;
    private Map<Long, Source> srcCache;
    private IMap<XDMIndexKey, XDMIndexedValue> idxCache;
    private IMap<XDMDataKey, XDMElements> xdmCache;
	private Map<String, XQProcessor> processors = new ConcurrentHashMap<String, XQProcessor>();

    
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.appContext = context;
	}

	public void setDocumentIdGenerator(IdGenerator docGen) {
    	this.docGen = docGen;
    }
    
    public void setIdxCache(IMap<XDMIndexKey, XDMIndexedValue> cache) {
    	this.idxCache = cache;
    }

    public void setXddCache(IMap<Long, XDMDocument> cache) {
    	this.xddCache = cache;
    }

    public void setXdmCache(IMap<XDMDataKey, XDMElements> cache) {
    	this.xdmCache = cache;
    }

    public void setXmlCache(IMap<Long, String> cache) {
    	this.xmlCache = cache;
    	this.srcCache = new ConcurrentHashMap<Long, Source>();
    }
    
    //@Autowired
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
		hzInstance.getClientService().addClientListener(this);
		logger.debug("setHzInstange; got instance: {}", hzInstance.getName()); 
	}
	
    public void setQueryManager(XDMQueryManagement xqManager) {
    	this.queryManager = xqManager;
    }
    
    @Override
	public Collection<String> buildDocument(Set<Long> docIds, String template, Map<String, String> params) {
        logger.trace("buildDocument.enter; docIds: {}", docIds.size());
		long stamp = System.currentTimeMillis();
        Collection<String> result = new ArrayList<String>(docIds.size());
		
		for (Iterator<Long> itr = docIds.iterator(); itr.hasNext(); ) {
			Long docId = itr.next();
			if (hzInstance.getPartitionService().getPartition(docId).getOwner().localMember()) {
				StringBuilder buff = new StringBuilder(template);
				for (Map.Entry<String, String> param: params.entrySet()) {
					String key = param.getKey();
					XDMDocument doc = xddCache.get(docId);
					String str = buildElement(xdmCache, param.getValue(), doc.getDocumentId(), doc.getTypeId());
					while (true) {
						int idx = buff.indexOf(key);
				        //logger.trace("aggregate; searching key: {} in buff: {}; result: {}", new Object[] {key, buff, idx});
						if (idx < 0) break;
						buff.replace(idx, idx + key.length(), str);
				        //logger.trace("aggregate; replaced key: {} with {}", key, str);
					}
				}
				result.add(buff.toString());
			} else {
				// remove is not supported by the HZ iterator provided! 
				// actually, don't think we have to do it at all..
				//itr.remove();
		        logger.debug("buildDocument; docId {} is not local, processing skipped", docId);
			}
		}
        
		stamp = System.currentTimeMillis() - stamp;
        logger.trace("buildDocument.exit; time taken: {}; returning: {}", stamp, result.size()); 
        return result;
	}
    
    private Set<XDMDataKey> getDocumentElementKeys(String path, long docId, int docType) {
    	Set<Integer> parts = mDictionary.getPathElements(docType, path);
    	Set<XDMDataKey> keys = new HashSet<XDMDataKey>(parts.size());
    	// not all the path keys exists as data key for particular document!
    	for (Integer part: parts) {
    		keys.add(mFactory.newXDMDataKey(docId, part));
    	}
    	return keys;
    }

    private String buildElement(IMap dataMap, String path, long docId, int docType) {
    	Set<XDMDataKey> xdKeys = getDocumentElementKeys(path, docId, docType);
       	return buildXml(dataMap.getAll(xdKeys));
    }
    
    public XDMDocument createDocument(String uri, String xml) {
    	
		long docId = docGen.next(); 
		return createDocument(new AbstractMap.SimpleEntry(docId, null), uri, xml);
    }
    
	@Override
	public XDMDocument createDocument(Entry<Long, XDMDocument> entry, String uri, String xml) {
		logger.trace("createDocument.enter; entry: {}", entry);

		XDMStaxParser parser = new XDMStaxParser(mDictionary);
		List<XDMData> data;
		try {
			data = parser.parse(xml);
		} catch (IOException | XMLStreamException ex) {
			// TODO: move this out & refactor
			logger.debug("createDocument.error", ex); 
			throw new IllegalArgumentException(ex);
		}
		
		Long docId = entry.getKey();
		int docType = loadElements(docId, data); 
		if (docType >= 0) {
			String user = JMXUtils.getCurrentUser();
			XDMDocument doc = new XDMDocument(docId, uri, docType, user); // + version, createdAt, encoding
			xddCache.set(docId, doc);
			xmlCache.set(docId, xml);
			logger.trace("createDocument.exit; returning: {}", doc);
			return doc;
		} else {
			logger.warn("createDocument.exit; the document is not valid as it has no root element, returning null");
			return null;
		}
	}
	
	public int loadElements(long docId, List<XDMData> data) {
		
		long stamp = System.currentTimeMillis();
		XDMData root = getDataRoot(data);
		if (root != null) {
			int docType = mDictionary.translateDocumentType(root.getPath());
			Map<XDMDataKey, XDMElements> elements = new HashMap<XDMDataKey, XDMElements>(data.size());
			Map<XDMIndexKey, XDMIndexedValue> indexes = new HashMap<XDMIndexKey, XDMIndexedValue>();
			
			for (XDMData xdm: data) {
				XDMDataKey xdk = mFactory.newXDMDataKey(docId, xdm.getPathId());
				XDMElements xdes = elements.get(xdk);
				if (xdes == null) {
					xdes = new XDMElements(xdk.getPathId(), null);
					elements.put(xdk, xdes);
				}
				xdes.addElement(xdm.getElement());
				
				// handle indexes
				if (isPathIndexed(xdm.getPathId()) && xdm.getValue() != null) {
					XDMIndexKey xid = mFactory.newXDMIndexKey(xdm.getPathId(), xdm.getValue());
					XDMIndexedValue xidx = indexes.get(xid);
					if (xidx == null) {
						xidx = new XDMIndexedValue(docId);
						indexes.put(xid, xidx);
					} else {
						xidx.addDocumentId(docId);
					}
				}
			}
			xdmCache.putAll(elements);
			idxCache.putAll(indexes);
			stamp = System.currentTimeMillis() - stamp;
			logger.trace("loadElements; cached {} elements and {} indexes for docId: {}; time taken: {}", 
					elements.size(), indexes.size(), docId, stamp);
			mDictionary.normalizeDocumentType(docType);
			return docType;
		}
		return XDMSchemaDictionaryBase.WRONG_PATH;
	}
	
	private boolean isPathIndexed(int pathId) {
		return mDictionary.isPathIndexed(pathId);
	}

	@Override
	public void deleteDocument(Entry<Long, XDMDocument> entry) {

		Long docId = entry.getKey();
		logger.trace("deleteDocument.enter; docId: {}", docId);
	    boolean removed = false;
	    XDMDocument doc = xddCache.remove(docId);
	    if (doc != null) {
	    	deleteDocumentElements(docId, doc.getTypeId());
			xmlCache.delete(docId);
			srcCache.remove(docId);
	        removed = true;
	    //} else { 
		//	throw new IllegalStateException("Document Entry with id " + entry.getKey() + " not found");
	    }
		logger.trace("deleteDocument.exit; removed: {}", removed);
	}
	
	private void deleteDocumentElements(long docId, int typeId) {

    	int cnt = 0;
    	//Set<XDMDataKey> localKeys = xdmCache.localKeySet();
    	int localSize = xdmCache.localKeySet().size();
    	Collection<XDMPath> allPaths = mDictionary.getTypePaths(typeId);
		logger.trace("deleteDocumentElements; got {} possible paths to remove; xdmCache size: {}; local keys: {}", 
				allPaths.size(), xdmCache.size(), localSize);
        for (XDMPath path: allPaths) {
        	XDMDataKey key = mFactory.newXDMDataKey(docId, path.getPathId()); 
       		xdmCache.delete(key);
        	cnt++;
        	// TODO: remove indexed values!
        }
    	localSize -= xdmCache.localKeySet().size();
		logger.trace("deleteDocumentElements; deleted keys: {}; xdmCache size after delete: {}; local size: {}", cnt, 
				xdmCache.size(), localSize);
	}

	@Override
	public XDMDocument updateDocument(Entry<Long, XDMDocument> entry, boolean newVersion, String xml) {
		// TODO: not implemented yet
		return null;
	}

	@Override
	protected Set<Long> queryPathKeys(Set<Long> found, PathExpression pex, Object value) {

		logger.trace("queryPathKeys.enter; found: {}", found == null ? "null" : found.size()); 
		Predicate pp = null;
		int pathId = 0;
		if (pex.isRegex()) {
			Set<Integer> pathIds = mDictionary.translatePathFromRegex(pex.getDocType(), pex.getRegex());
			logger.trace("queryPathKeys; regex: {}; pathIds: {}", pex.getRegex(), pathIds);
			if (pathIds.size() > 0) {
				// ?? use all of them !
				pp = Predicates.in("pathId", pathIds.toArray(new Integer[pathIds.size()]));
			}
		} else {
			String path = pex.getFullPath();
			logger.trace("queryPathKeys; path: {}; comparison: {}", path, pex.getCompType());
			XDMPath xPath = mDictionary.translatePath(pex.getDocType(), path, XDMNodeKind.fromPath(path));
			pp = Predicates.equal("pathId", xPath.getPathId());
			pathId = xPath.getPathId();
		}
		
		if (pp == null) {
			throw new IllegalArgumentException("Path not found for expression: " + pex);
		}
		
   		//try {
		//	long stamp = System.currentTimeMillis(); 
	   	//	Supplier<XDMDataKey, XDMElements, Object> supplier = Supplier.fromKeyPredicate(new DataKeyPredicate(pathId));
	   	//	Aggregation<XDMDataKey, Object, Long> aggregation = Aggregations.count();
	   	//	Long count = xdmCache.aggregate(supplier, aggregation);
	   	//	stamp = System.currentTimeMillis() - stamp;
		//	logger.info("queryPathKeys; got {} aggregation results; time taken: {}", count, stamp); 
   		//} catch (Throwable ex) {
   		//	logger.error("queryPathKeys", ex);
   		//}
		
		if (pathId > 0 && Comparison.EQ.equals(pex.getCompType()) && 
				isPathIndexed(pathId)) {
			XDMIndexKey idx = mFactory.newXDMIndexKey(pathId, value.toString());
			XDMIndexedValue xidx = idxCache.get(idx);
			logger.trace("queryPathKeys; seach for index {} get {}", idx, xidx); 
			if (xidx != null) {
				Set<Long> result;
				if (found == null) {
					result = xidx.getDocumentIds();
				} else {
					found.retainAll(xidx.getDocumentIds());
					result = found;
				}
				logger.trace("queryPathKeys.exit; returning {} indexed keys", result.size()); 
				return result;
			}
		}
   		
   		Predicate<XDMDataKey, XDMElements> f = Predicates.and(pp, new QueryPredicate(pex, value));
   		Set<XDMDataKey> xdmKeys = xdmCache.keySet(f);
		logger.trace("queryPathKeys; got {} query results", xdmKeys.size()); 
		Set<Long> result = new HashSet<Long>(xdmKeys.size());
		if (found == null) {
			for (XDMDataKey key: xdmKeys) {
				result.add(key.getDocumentId());
			}
		} else {
			for (XDMDataKey key: xdmKeys) {
				long docId = key.getDocumentId();
				if (found.contains(docId)) {
					result.add(docId);
				}
			}
		}
		logger.trace("queryPathKeys.exit; returning {} keys", result.size()); 
		return result;
	}

	@Override
	public Collection<Long> getDocumentIDs(ExpressionContainer query) {
		ExpressionBuilder exp = query.getExpression();
		if (exp.getRoot() != null) {
			return queryKeys(null, query, exp.getRoot());
		}
		logger.info("getDocumentIDs; got rootless path: {}", query); 
		// can we use local keySet only !?
		return xddCache.keySet();
	}
	
	@Override
	public Collection<String> getDocumentURIs(ExpressionContainer query) {
		// TODO: remove this method completely, or
		// make reverse cache..? or, make URI from docId somehow..
		Collection<Long> ids = getDocumentIDs(query);
		Set<String> result = new HashSet<String>(ids.size());
		// TODO: better to do this via EP or aggregator?
		Map<Long, XDMDocument> docs = xddCache.getAll(new HashSet<Long>(ids));
		for (XDMDocument doc: docs.values()) {
			result.add(doc.getUri());
		}
		return result;
	}

	@Override
	public XDMDocument getDocument(long docId) {
		XDMDocument doc = xddCache.get(docId); 
		logger.trace("getDocument; returning: {}", doc);
		return doc;
	}

	@Override
	public Long getDocumentId(String uri) {
   		Predicate<Long, XDMDocument> f = Predicates.equal("uri", uri);
		Set<Long> docKeys = xddCache.keySet(f);
		if (docKeys.size() == 0) {
			return null;
		}
		// todo: check if too many docs ??
		return docKeys.iterator().next();
	}
	
	@Override
	public String getDocumentAsString(long docId) {
		String xml = xmlCache.get(docId);
		if (xml == null) {
			XDMDocument doc = getDocument(docId);
			if (doc == null) {
				logger.info("getDocumentAsString; no document found for ID: {}", docId);
				return null;
			}
			
			// if docId is not local then buildDocument returns null!
			// query docId owner node for the XML instead
			if (hzInstance.getPartitionService().getPartition(docId).getOwner().localMember()) {
				String root = mDictionary.getDocumentRoot(doc.getTypeId());
				Map<String, String> params = new HashMap<String, String>();
				params.put(":doc", root);
				Collection<String> results = buildDocument(Collections.singleton(docId), ":doc", params);
				if (!results.isEmpty()) {
					xml = results.iterator().next();
					xmlCache.set(docId, xml);
				}
			} else {
				XMLProvider xp = new XMLProvider(docId);
				IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SCHEMA_POOL);
				Future<String> future = execService.submitToKeyOwner(xp, docId);
				try {
					xml = future.get();
				} catch (InterruptedException | ExecutionException ex) {
					logger.error("getDocumentAsString; error getting result", ex);
					// rethrow ex?
				}
			}
		}
		return xml;
	}

	@Override
	public Source getDocumentAsSource(long docId) {
		return srcCache.get(docId);
	}
	
	@Override
	public XDMDocument storeDocumentSource(long docId, Source source) {
		srcCache.put(docId, source);
		return xddCache.get(docId);
	}
	
	@Override
	public XDMDocument storeDocument(String xml) {

	    return storeDocument(0, null, xml);
	}

	@Override
	public XDMDocument storeDocument(long docId, String uri, String xml) {
		// create new document version ??
		// what if we want to pass here correct URI ??
		logger.trace("storeDocument.enter; docId: {}; uri: {}; xml: {}", docId, uri, xml.length());

		boolean update = false;
		if (docId == 0) {
			docId = docGen.next();
		} else {
			update = true;
		}
		
		if (uri == null) {
			uri = "" + docId + ".xml";
		} else {
			Long existingId = getDocumentId(uri);
			if (existingId != null && existingId != docId) {
				// otherwise we'll get a situation when two different Documents
				// are stored in the same file.
				throw new IllegalArgumentException("Document with URI '" + uri + "' already exists; docId: " + existingId);
			}
		}
		
		if (update) {
		    XDMDocument doc = xddCache.get(docId);
		    if (doc != null) {
		    	deleteDocumentElements(docId, doc.getTypeId());
		    }
		}
	    return createDocument(new AbstractMap.SimpleEntry(docId, null), uri, xml);
	    // go to updateDocument ..?
	}

	@Override
	public void removeDocument(long docId) {
		deleteDocument(new AbstractMap.SimpleEntry(docId, null));
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}

	@Override
	public Collection<String> getXML(ExpressionContainer query, String template, Map params) {
		Collection<Long> docIds = getDocumentIDs(query);
		if (docIds.size() > 0) {
			return buildDocument(new HashSet<Long>(docIds), template, params);
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public Object executeXCommand(String command, Map bindings, Properties props) {
		
		return execXQCommand(false, command, bindings, props);
	}

	@Override
	public Object executeXQuery(String query, Map bindings, Properties props) {

		return execXQCommand(true, query, bindings, props);
	}

	private HazelcastXQCursor createCursor(String clientId, int batchSize, Iterator iter, boolean failure) {
		final HazelcastXQCursor xqCursor = new HazelcastXQCursor(clientId, batchSize, iter, failure);
		
		// TODO: put everything to the queue - can be too slow!
		// think of async solution..
		// profile: it takes 3.19 ms to serialize!
		// another profile session: 13 ms spent in toData conversion!
		// 12.5 ms: SaxonXQProcessor.convertToString -> net.sf.saxon.query.QueryResult.serializeSequence (11 ms)

		// but async serialization takes even more time! because of the thread context switch, most probably
		//IExecutorService execService = hzInstance.getExecutorService(PN_XDM_SCHEMA_POOL);
		//execService.execute(new Runnable() {
		//	@Override
		//	public void run() {
		//		xqCursor.serialize(hzInstance);
		//	}
		//});
		
		xqCursor.serialize(hzInstance);
		return xqCursor;
	}
	
	private Object execXQCommand(boolean isQuery, String xqCmd, Map bindings, Properties props) {

		logger.trace("execXQCommand.enter; query: {}, command: {}; bindings: {}", isQuery, xqCmd, bindings);
		Object result = null;
		Iterator iter = null;
		String clientId = props.getProperty("clientId");
		int batchSize = Integer.parseInt(props.getProperty("batchSize", "0"));
		try {
			//if (isQuery) {
			//	iter = queryManager.getQueryResults(xqCmd, bindings, props);
			//}
			
			if (iter == null) {
				XQProcessor xqp = getXQProcessor(clientId);
				for (Object o: bindings.entrySet()) {
					Map.Entry<QName, Object> var = (Map.Entry<QName, Object>) o; 
					xqp.bindVariable(var.getKey(), var.getValue());
				}
				
				if (isQuery) {
					iter = xqp.executeXQuery(xqCmd, props);
				} else {
					iter = xqp.executeXCommand(xqCmd, bindings, props);
				}
				
				for (Object o: bindings.entrySet()) {
					Map.Entry<QName, Object> var = (Map.Entry<QName, Object>) o; 
					xqp.unbindVariable(var.getKey());
				}
				
			//	iter = queryManager.addQueryResults(xqCmd, bindings, props, iter);
			}
			result = createCursor(clientId, batchSize, iter, false);
		} catch (Throwable ex) {
			logger.error("execXQCommand.error;", ex);
			result = createCursor(clientId, batchSize, new BagriExceptionIterator(ex), true);
		}
		logger.trace("execXQCommand.exit; returning: {}, for client: {}", result, clientId);
		return result;
	}

	@Override
	public void clientConnected(Client client) {
		logger.trace("clientConnected.enter; client: {}", client); 
		// create queue
		IQueue queue = hzInstance.getQueue("client:" + client.getUuid());
		// create/cache new XQProcessor
		XQProcessor proc = getXQProcessor(client.getUuid());
		logger.trace("clientConnected.exit; queue {} created for client: {}; XQProcessor: {}", 
				queue.getName(), client.getSocketAddress(), proc);
	}

	@Override
	public void clientDisconnected(Client client) {
		logger.trace("clientDisconnected.enter; client: {}", client);
		String qName = "client:" + client.getUuid();
		boolean destroyed = false;
		Collection<DistributedObject> all = hzInstance.getDistributedObjects();
		int sizeBefore = all.size();
		for (DistributedObject obj: all) {
			if (qName.equals(obj.getName())) {
				// remove queue
				IQueue queue = hzInstance.getQueue(qName);
				queue.destroy();
				destroyed = true;
				break;
			}
		}
		int sizeAfter = hzInstance.getDistributedObjects().size(); 
		XQProcessor proc = processors.remove(client.getUuid());
		logger.trace("clientDisconnected.exit; queue {} {} for client: {}; size before: {}, after: {}", 
				qName, destroyed ? "destroyed" : "skipped", client.getSocketAddress(), sizeBefore, sizeAfter); 
	}
	
	private XQProcessor getXQProcessor(String clientId) {
		XQProcessor result = processors.get(clientId);
		if (result == null) {
			result = appContext.getBean(XQProcessor.class, this);
			//QueryManagementServer qMgr = appContext.getBean(QueryManagementServer.class);
			result.setXQManager(queryManager);
			((BagriXQDataFactory) ((BagriXQProcessor) result).getXQDataFactory()).setProcessor(result);
			processors.put(clientId, result);
		}
		logger.trace("getXQProcessor; returning: {}", result);
		return result;
	}

}
