package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.*;
import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_TRANSACTION;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_PopulateSchemaTask;

import java.util.Properties;
import java.util.concurrent.Callable;

import org.springframework.context.ApplicationContext;

import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.cache.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.TransactionManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.XDMFactoryImpl;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMTransaction;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.Partition;
import com.hazelcast.core.PartitionService;

public class SchemaPopulator extends SchemaProcessingTask implements Callable<Boolean> {
	
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
		//hz = hzInstance;
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

    	logger.debug("populateSchema.enter; HZ instance: {}", hz);

		ApplicationContext schemaCtx = (ApplicationContext) getContext(schemaName, schema_context);
		//if (schemaCtx == null) {
		//	schemaCtx = (ApplicationContext) hz.getUserContext().get("appContext");
		//}
		if (schemaCtx == null) {
	    	logger.info("populateSchema.exit; No Spring Context initialized yet");
			return false;
		}

		//ApplicationContext storeCtx = (ApplicationContext) getContext(schemaName, store_context);
		//if (storeCtx == null) {
			// schema configured with no persistent store
	    //	logger.debug("populateSchema.exit; No persistent store configured");
		//	return false;
		//}
		
		//MapLoader docCacheStore = storeCtx.getBean("docCacheStore", MapLoader.class);
		
		//Properties props = new Properties();
		//props.put("documentIdGenerator", schemaCtx.getBean("xdm.document"));
		//props.put("keyFactory", schemaCtx.getBean(XDMFactoryImpl.class));
		//props.put("xdmModel", schemaCtx.getBean(XDMModelManagement.class));
		//props.put("xdmManager", schemaCtx.getBean(DocumentManagementImpl.class));
		//((MapLoaderLifecycleSupport) docCacheStore).init(hz, props, CN_XDM_DOCUMENT);
		
		IMap<Long, XDMDocument> xddCache = hz.getMap(CN_XDM_DOCUMENT);
		xddCache.loadAll(false);
    	logger.info("populateSchema; documents size after loadAll: {}", xddCache.size());

		IMap<Long, XDMTransaction> xtxCache = hz.getMap(CN_XDM_TRANSACTION);
		xtxCache.loadAll(false);
    	logger.info("populateSchema; transactions size after loadAll: {}", xtxCache.size());

    	// adjusting tx idGen!
		TransactionManagementImpl txMgr = schemaCtx.getBean("txManager", TransactionManagementImpl.class);
		txMgr.adjustTxCounter();
    	
    	return true;
	}
	
	@Override
	public int getId() {
		return cli_PopulateSchemaTask;
	}

}
