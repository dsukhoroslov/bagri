package com.bagri.xdm.cache.hazelcast.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.cache.api.XDMIndexManagement;
import com.bagri.xdm.cache.api.XDMRepository;
import com.bagri.xdm.client.common.impl.XDMRepositoryBase;
import com.bagri.xdm.domain.XDMPath;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.api.XQProcessor;
import com.bagri.xquery.saxon.XQProcessorServer;
import com.hazelcast.core.Client;
import com.hazelcast.core.ClientListener;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class RepositoryImpl extends XDMRepositoryBase implements ApplicationContextAware, ClientListener, XDMRepository {

	private static final transient Logger logger = LoggerFactory.getLogger(RepositoryImpl.class);
	
	private XDMSchema xdmSchema;
    private XDMIndexManagement indexMgr;
    private ApplicationContext appContext;
    private HazelcastInstance hzInstance;
	private Map<String, XQProcessor> processors = new ConcurrentHashMap<String, XQProcessor>();

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.appContext = context;
	}

    //@Autowired
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
		hzInstance.getClientService().addClientListener(this);
		logger.debug("setHzInstange; got instance: {}", hzInstance.getName()); 
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
	
	HazelcastInstance getHzInstance() {
		return hzInstance;
	}
	
	XQProcessor getXQProcessor(String clientId) {
		XQProcessor result = processors.get(clientId);
		if (result == null) {
			result = appContext.getBean(XQProcessor.class, this);
			XDMQueryManagement qMgr = getQueryManagement();
			result.setRepository(this);
			((BagriXQDataFactory) ((XQProcessorServer) result).getXQDataFactory()).setProcessor(result);
			processors.put(clientId, result);
		}
		logger.trace("getXQProcessor; returning: {}", result);
		return result;
	}

	@Override
	public void close() {
		// TODO: disconnect all clients ?
	}

	@Override
	public XDMSchema getSchema() {
		return xdmSchema;
	}
	
	public void setSchema(XDMSchema xdmSchema) {
		// TODO: think about run-time updates..
		this.xdmSchema = xdmSchema;
		Set<XDMIndex> indexes = xdmSchema.getIndexes();
		if (indexes.size() > 0) {
			for (XDMIndex idx: indexes) {
				indexMgr.createIndex(idx);
			}
		}
	}
	
	public boolean addSchemaIndex(XDMIndex index) {
		
		if (xdmSchema.addIndex(index)) {
			XDMPath path = indexMgr.createIndex(index);
			DocumentManagementImpl docMgr = (DocumentManagementImpl) getDocumentManagement();
			return docMgr.indexElements(path.getTypeId(), path.getPathId()) > 0;
		}
		logger.info("call; index {} already exists! do we need to index values?", index);
		return false;
	}

	public boolean dropSchemaIndex(String name) {
		
		XDMIndex index = xdmSchema.removeIndex(name);
		if (index != null) {
			XDMPath path = indexMgr.deleteIndex(index);
			if (path != null) {
				DocumentManagementImpl docMgr = (DocumentManagementImpl) getDocumentManagement();
				return docMgr.deindexElements(path.getTypeId(), path.getPathId()) > 0;
			}
		}
		logger.info("call; index {} does not exist?", index);
		return false;
	}
	
	@Override
	public XDMIndexManagement getIndexManagement() {
		return indexMgr;
	}

	public void setIndexManagement(XDMIndexManagement indexMgr) {
		this.indexMgr = indexMgr;
	}
	
	//@Override
	//public XDMModuleManagement getModuleManagement() {
	//	return moduleMgr;
	//}

	//public void setModuleManagement(XDMModuleManagement moduleMgr) {
	//	this.moduleMgr = moduleMgr;
	//}
	
	public boolean addSchemaModule(XDMModule module) {
		
		if (xdmSchema.addModule(module)) {
			//XDMPath path = indexMgr.createIndex(index);
			//DocumentManagementImpl docMgr = (DocumentManagementImpl) getDocumentManagement();
			return true; //docMgr.indexElements(path.getTypeId(), path.getPathId()) > 0;
		}
		return false;
	}

	public boolean dropSchemaModule(String name) {
		
		XDMModule module = xdmSchema.removeModule(name);
		if (module != null) {
			//XDMPath path = indexMgr.deleteIndex(index);
			//if (path != null) {
			//	DocumentManagementImpl docMgr = (DocumentManagementImpl) getDocumentManagement();
				return true; //docMgr.deindexElements(path.getTypeId(), path.getPathId()) > 0;
			//}
		}
		return false;
	}
	
}

