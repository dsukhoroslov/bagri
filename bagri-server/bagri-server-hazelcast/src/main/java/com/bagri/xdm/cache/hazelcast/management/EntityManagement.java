package com.bagri.xdm.cache.hazelcast.management;

import static com.bagri.xdm.common.XDMConstants.xdm_cluster_login;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;

import com.bagri.common.util.JMXUtils;
import com.bagri.xdm.system.Entity;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.Member;

public abstract class EntityManagement<E extends Entity> implements EntryListener<String, E>, InitializingBean {
	
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
        	logger.trace("afterPropertiesSet; initiating entity: {}", name);
        	initEntityManager(name);
        }
		logger.trace("afterPropertiesSet.exit; initiated {} entity managers", names.size());
	}
	
	public void setEntityCache(IMap<String, E> entityCache) {
		this.entityCache = entityCache;
		this.entityCache.addEntryListener(this, false);
	}
	
	protected String getCurrentUser() {
		return JMXUtils.getCurrentUser(((Member) hzInstance.getLocalEndpoint()).getStringAttribute(xdm_cluster_login));
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
	
	protected String[] getEntityNames() {
		Set<String> names = entityCache.keySet();
		return names.toArray(new String[names.size()]);
	}
	
	protected TabularData getEntities(String name, String desc) {
		Collection<E> entities = entityCache.values();
		if (entities.size() == 0) {
			return null;
		}
		
        TabularData result = null;
        for (Entity entity: entities) {
            try {
                Map<String, Object> def = entity.convert();
                CompositeData data = JMXUtils.mapToComposite(name, desc, def);
                result = JMXUtils.compositeToTabular(name, desc, "name", result, data);
            } catch (Exception ex) {
                logger.error("getEntities; error", ex);
            }
        }
        return result;
    }
	
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
