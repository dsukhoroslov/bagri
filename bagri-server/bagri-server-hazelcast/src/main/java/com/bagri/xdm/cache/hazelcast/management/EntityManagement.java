package com.bagri.xdm.cache.hazelcast.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;

import com.bagri.xdm.api.XDMEntity;
import com.bagri.xdm.system.XDMNode;
import com.bagri.xdm.system.XDMUser;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;

public abstract class EntityManagement<String, E extends XDMEntity> implements EntryListener<String, E>, InitializingBean {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	protected IMap<String, E> entityCache; 
    protected HazelcastInstance hzInstance;
    //not sure I have to do this here
    //protected Map<String, EntityManager<E>> mgrCache = new HashMap<String, EntityManager<E>>(); 
    protected Map<String, EntityManager<E>> mgrCache = new HashMap<>(); 

    @Autowired
	protected AnnotationMBeanExporter mbeanExporter;
    
	public EntityManagement(HazelcastInstance hzInstance) {
		//super();
		this.hzInstance = hzInstance;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		logger.trace("afterPropertiesSet.enter");
        Set<String> names = entityCache.keySet();
        for (String name: names) {
        	initEntityManager(name);
        }
		logger.trace("afterPropertiesSet.exit; initiated {} entity managers", names.size());
	}
	
	public void setEntityCache(IMap<String, E> entityCache) {
		this.entityCache = entityCache;
		this.entityCache.addEntryListener(this, false);
	}
	
	protected EntityManager<E> initEntityManager(String entityName) throws MalformedObjectNameException {
		EntityManager<E> eMgr = null;
   	    if (!mgrCache.containsKey(entityName)) {
			eMgr = createEntityManager(entityName);
			//eMgr.setEntityCache(entityCache);
			mgrCache.put(entityName, eMgr);
			mbeanExporter.registerManagedResource(eMgr, eMgr.getObjectName());
		}
   	    return eMgr;
	}
	
	protected abstract EntityManager<E> createEntityManager(String entityName);
	
	public Collection<E> getEntities() {
		return new ArrayList<E>(entityCache.values());
	}
	
	public EntityManager<E> getEntityManager(String entityName) {
		return mgrCache.get(entityName);
	}
	
	//public String[] getEntityNames() {
	//	Set<String> names = entityCache.keySet();
	//	return names.toArray(new String[names.size()]);
	//}
	
	@Override
	public void entryAdded(EntryEvent<String, E> event) {
		logger.trace("entryAdded; event: {}", event);
		String entityName = event.getKey();
		try {
			initEntityManager(entityName);
		} catch (MBeanExportException | MalformedObjectNameException ex) {
			// JMX registration failed.
			logger.error("entryAdded.error: ", ex);
		}
	}

	@Override
	public void entryRemoved(EntryEvent<String, E> event) {
		logger.trace("entryRemoved; event: {}", event);
		String entityName = event.getKey();
		EntityManager<E> eMgr = mgrCache.remove(entityName);
		try {
			mbeanExporter.unregisterManagedResource(eMgr.getObjectName());
		} catch (MalformedObjectNameException ex) {
			logger.error("entryRemoved.error: ", ex);
		}
	}

	@Override
	public void entryUpdated(EntryEvent<String, E> event) {
		logger.trace("entryUpdated; event: {}", event);
	}

	@Override
	public void entryEvicted(EntryEvent<String, E> event) {
		logger.trace("entryEvicted; event: {}", event);
		// make entity inactive ?
	}
	
	@Override
	public void mapEvicted(MapEvent event) {
		logger.trace("mapEvicted; event: {}", event);
		// make entity inactive ?
	}

	@Override
	public void mapCleared(MapEvent event) {
		logger.trace("mapCleared; event: {}", event);
		// shouldn't be this! delete all roles?
	}

}
