package com.bagri.server.hazelcast.task.schema;

import static com.bagri.core.Constants.ctx_popService;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_TRANSACTION;
import static com.bagri.core.server.api.CacheConstants.TPN_XDM_POPULATION;
import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_PopulateSchemaTask;
import static com.bagri.server.hazelcast.util.SpringContextHolder.*;

import java.util.concurrent.Callable;

import org.springframework.context.ApplicationContext;

import com.bagri.core.model.Document;
import com.bagri.core.model.Transaction;
import com.bagri.server.hazelcast.impl.PopulationManagementImpl;
import com.bagri.server.hazelcast.impl.TransactionManagementImpl;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;

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
		// get hzInstance 
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

    	logger.debug("populateSchema.enter; HZ instance: {}", hz);

		ApplicationContext schemaCtx = getContext(schemaName);
		if (schemaCtx == null) {
	    	logger.info("populateSchema.exit; No Spring Context initialized yet");
			return false;
		}

		IMap<Long, Transaction> xtxCache = hz.getMap(CN_XDM_TRANSACTION);
		xtxCache.loadAll(false);
    	logger.info("populateSchema; transactions size after loadAll: {}", xtxCache.size());

		IMap<Long, Document> xddCache = hz.getMap(CN_XDM_DOCUMENT);
		xddCache.loadAll(false);
    	logger.info("populateSchema; documents size after loadAll: {}", xddCache.size());

		ITopic<Long> pTopic = hz.getTopic(TPN_XDM_POPULATION);
		PopulationManagementImpl pm = (PopulationManagementImpl) hz.getUserContext().get(ctx_popService);
		int lo = pm.getActiveCount();
		int hi = pm.getDocumentCount() - lo;
		long counts = ((long) hi << 32) + lo;
		pTopic.publish(counts);

    	// adjusting tx idGen!
		TransactionManagementImpl txMgr = schemaCtx.getBean("txManager", TransactionManagementImpl.class);
		txMgr.adjustTxCounter(pm.getMaxTransactionId());

    	return true;
	}
	
	@Override
	public int getId() {
		return cli_PopulateSchemaTask;
	}

}
