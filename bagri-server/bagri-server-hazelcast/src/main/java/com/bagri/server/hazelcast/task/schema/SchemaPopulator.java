package com.bagri.server.hazelcast.task.schema;

import static com.bagri.core.Constants.ctx_popService;
import static com.bagri.core.server.api.CacheConstants.TPN_XDM_POPULATION;
import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_PopulateSchemaTask;
import static com.bagri.server.hazelcast.util.HazelcastUtils.findSchemaInstance;
import static com.bagri.server.hazelcast.util.SpringContextHolder.*;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.springframework.context.ApplicationContext;

import com.bagri.server.hazelcast.impl.PopulationManagementImpl;
import com.bagri.server.hazelcast.impl.TransactionManagementImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class SchemaPopulator extends SchemaProcessingTask implements Callable<Boolean> {
	
	private boolean stopPopulation = false;
	private boolean overrideExisting = false;
	
	public SchemaPopulator() {
		super();
	}
	
	public SchemaPopulator(String schemaName, boolean overrideExisting, boolean stopPopulation) {
		super(schemaName);
		this.overrideExisting = overrideExisting;
		this.stopPopulation = stopPopulation;
	}

	@Override
	public Boolean call() throws Exception {
    	logger.debug("call.enter; schema: {}", schemaName);
    	boolean result = false;
		// get hzInstance 
		HazelcastInstance hz = findSchemaInstance(schemaName);
		if (hz != null) {
			if (stopPopulation) {
				PopulationManagementImpl pm = (PopulationManagementImpl) hz.getUserContext().get(ctx_popService);
				pm.stopPopulation();
			} else {
				// ensure the partitions migration has been already finished! 
				if (hz.getPartitionService().isClusterSafe()) {
					try {
						result = populateSchema(hz);
						// now can turn triggers on.. but we need it even without population..
				    	if (result) {
				    		logger.info("call.exit; schema {} populated", schemaName);
				    	} else {
				    		logger.info("call.exit; schema {} population started", schemaName);
				    	}
					} catch (Exception ex) {
				    	logger.error("call.error; on schema population", ex);
					}
				} else {
			    	logger.info("call.exit; cluster is in {} state, skipping population", hz.getCluster().getClusterState());
				}
			}
		}
		return result;
	}

	private boolean populateSchema(HazelcastInstance hz) {

    	logger.debug("populateSchema.enter; HZ instance: {}", hz);

		ApplicationContext schemaCtx = getContext(schemaName);
		if (schemaCtx == null) {
	    	logger.info("populateSchema.exit; No Spring Context initialized yet");
			return false;
		}
		
		PopulationManagementImpl pm = (PopulationManagementImpl) hz.getUserContext().get(ctx_popService);
		if (pm.populateSchema(overrideExisting)) {
			// move this code to PM ?
			ITopic<Long> pTopic = hz.getTopic(TPN_XDM_POPULATION);
			int lo = pm.getActiveCount();
			int hi = pm.getDocumentCount() - lo;
			long counts = ((long) hi << 32) + lo;
			pTopic.publish(counts);
	
	    	// adjusting tx idGen!
			TransactionManagementImpl txMgr = schemaCtx.getBean("txManager", TransactionManagementImpl.class);
			txMgr.adjustTxCounter(pm.getMaxTransactionId());
			return true;
		}
		return false;
	}
	
	@Override
	public int getId() {
		return cli_PopulateSchemaTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		overrideExisting = in.readBoolean();
		stopPopulation = in.readBoolean();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeBoolean(overrideExisting);
		out.writeBoolean(stopPopulation);
	}

}
