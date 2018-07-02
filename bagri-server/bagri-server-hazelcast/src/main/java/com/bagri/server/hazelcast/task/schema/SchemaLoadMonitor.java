package com.bagri.server.hazelcast.task.schema;

import static com.bagri.support.util.JMXUtils.mapToComposite;
import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_factory_id;
import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_MonitorSchemaLoadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.management.openmbean.CompositeData;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.server.hazelcast.impl.PopulationManagementImpl;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.core.Member;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaLoadMonitor implements Callable<CompositeData>, IdentifiedDataSerializable {

	private transient SchemaRepositoryImpl xdmRepo;
    
    @Autowired
	public void setXDMRepository(SchemaRepositoryImpl xdmRepo) {
		this.xdmRepo = xdmRepo;
	}

	@Override
	public CompositeData call() throws Exception {
		PopulationManagementImpl pSvc = (PopulationManagementImpl) xdmRepo.getPopulationManagement();
		Map<String, Object> result = new HashMap<>(1);
		result.put("StartedBatches", pSvc.getStartedBatchCount());
		result.put("FinishedBatches", pSvc.getFinishedBatchCount());
		result.put("Keys", pSvc.getKeyCount());
		result.put("Errors", pSvc.getErrorCount());
		result.put("Loading", pSvc.getLoadingCount());
		result.put("Loaded", pSvc.getLoadedCount());
		result.put("Loaders", pSvc.getLoadThreadCount());
		result.put("StartTime", pSvc.getStartTime());
		result.put("LastTime", pSvc.getLastTime());
		Member m = xdmRepo.getHzInstance().getCluster().getLocalMember();
		result.put("Member", m.getSocketAddress().toString()); 
		return mapToComposite("Population", "Monitor", result);
	}
	
	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_MonitorSchemaLoadTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		// TODO nothing to read
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		// nothing to write
	}


}
