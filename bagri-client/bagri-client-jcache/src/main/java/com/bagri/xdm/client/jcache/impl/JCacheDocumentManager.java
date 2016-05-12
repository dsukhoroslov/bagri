package com.bagri.xdm.client.jcache.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.domain.XDMElement;

/**
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 * @version 0.1
 */
public class JCacheDocumentManager {
	
    private static final Logger logger = LoggerFactory.getLogger(JCacheDocumentManager.class);
	
	private CacheManager cMgr;
	private String cn_data = "xdm.cache";
	private Cache<Long, XDMElement> xdmCache;
	
	// must be configurable...
	private int batchSize = 1000;
	
	public JCacheDocumentManager() {
		initialize();
	}
	
	private void initialize() {
		cMgr = Caching.getCachingProvider().getCacheManager();
		logger.trace("initialize; got cache manager: {}, with properties: {}", cMgr, cMgr.getProperties());
		xdmCache = cMgr.getCache(cn_data);
		if (xdmCache == null) {
			Configuration<Long, XDMElement> config = new MutableConfiguration<Long, XDMElement>();
			logger.trace("initialize; got configuration: {}", config);
			xdmCache = cMgr.createCache(cn_data, config);
		//	xdmCache = cMgr. <Long, XDMElement>createCacheBuilder(cn_data).build();
		}
		logger.trace("initialize; got cache: {}", xdmCache);
		//cMgr.enableManagement(cn_data, true);
		//cMgr.enableStatistics(cn_data, true);
	}
	
	public void store(XDMElement data) {
		//xdmCache.put(data.getElementId(), data);
	}

	public void store(Long id, XDMElement data) {
		xdmCache.put(id, data);
	}

	public void storeAll(Collection<XDMElement> data) {

		logger.trace("storeAll.enter; got {} entries", data.size());
		
		int size = Math.max(batchSize, data.size());
		int idx = 1;
		Map<Long, XDMElement> map = new HashMap<Long, XDMElement>(size);
		for (Iterator<XDMElement> itr = data.iterator(); itr.hasNext(); idx++) {
			XDMElement xdm = itr.next();
			//map.put(xdm.getElementId(), xdm);
			if (idx % size == 0) { 
				xdmCache.putAll(map);
				logger.trace("storeAll; stored {} entries", map.size());
				map.clear();
			}
		}

		if (map.size() > 0) {  
			xdmCache.putAll(map);
			logger.trace("storeAll; stored {} entries", map.size());
		}

		// no size method ??
		//map = xdmCache.getAll();
		//logger.trace("storeAll.exit; now cached {} entries", map.size());
	}

	public void storeAll(Map<Long, XDMElement> data) {
		
		xdmCache.putAll(data);
	}
	
	public Map<Long, XDMElement> execQuery(String query) {
		//
		logger.trace("execQuery.enter; got query: {}", query);
		//Map<Long, XDMData> result = xdmCache.getAll(null); 
		Map<Long, XDMElement> result = new HashMap<Long, XDMElement>();
		for (Iterator<Cache.Entry<Long, XDMElement>> itr=xdmCache.iterator(); itr.hasNext(); ) {
			Cache.Entry<Long, XDMElement> entry = itr.next();
			result.put(entry.getKey(), entry.getValue());
		}
		logger.trace("execQuery.exit; returning {} entries", result.size());
		return result;
	}

}
