package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_PopulateSchemaTask;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.springframework.context.ApplicationContext;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.cache.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.TransactionManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.XDMFactoryImpl;
import com.bagri.xdm.cache.hazelcast.util.SpringContextHolder;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;

public class SchemaPopulator extends SchemaDenitiator {
	
	public SchemaPopulator() {
		super();
	}
	
	public SchemaPopulator(String schemaName) {
		super(schemaName);
	}

	@Override
	public Boolean call() throws Exception {
    	logger.debug("call.enter; schema: {}", schemaName);
    	boolean result = false;
		// get hzInstance and close it...
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
		if (hz != null) {
			try {
				// TODO: ensure that partitions migration has been already finished! 
				result = populateSchema(hz);
				// now can turn triggers on.. but we need it even without population..
			} catch (Exception ex) {
		    	logger.error("call.error; on Schema population", ex);
			}
		}
    	logger.info("call.exit; schema {} populated: {}", schemaName, result);
		return result;
	}

	private boolean populateSchema(HazelcastInstance hz) {

    	logger.trace("populateSchema.enter; HZ instance: {}", hz);

		ApplicationContext schemaCtx = (ApplicationContext) SpringContextHolder.getContext(schemaName, "appContext");
		ApplicationContext storeCtx = (ApplicationContext) SpringContextHolder.getContext(schemaName, "storeContext");

		// adjusting tx idGen!
		TransactionManagementImpl txMgr = schemaCtx.getBean("txManager", TransactionManagementImpl.class);
		txMgr.adjustTxCounter();
		
		if (storeCtx == null) {
			// schema configured with no persistent store
	    	logger.trace("populateSchema.exit; No persistent store configured");
			return false;
		}
		
		MapLoader docCacheStore = storeCtx.getBean("docCacheStore", MapLoader.class);
		
		Properties props = new Properties();
		//props.put("documentIdGenerator", schemaCtx.getBean("xdm.document"));
		props.put("keyFactory", schemaCtx.getBean(XDMFactoryImpl.class));
		props.put("xdmModel", schemaCtx.getBean(XDMModelManagement.class));
		props.put("xdmManager", schemaCtx.getBean(DocumentManagementImpl.class));
		((MapLoaderLifecycleSupport) docCacheStore).init(hz, props, CN_XDM_DOCUMENT);
		
		IMap<Long, XDMDocument> xddCache = hz.getMap(CN_XDM_DOCUMENT);
		xddCache.loadAll(false);
    	logger.trace("populateSchema; documents size after loadAll: {}", xddCache.size());
		return true;
	}
	
	@Override
	public int getId() {
		return cli_PopulateSchemaTask;
	}

}
