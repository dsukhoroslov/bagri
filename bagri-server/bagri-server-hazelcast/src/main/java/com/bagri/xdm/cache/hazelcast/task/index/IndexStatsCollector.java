package com.bagri.xdm.cache.hazelcast.task.index;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_CollectIndexStaticsTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.management.openmbean.TabularData;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.cache.hazelcast.impl.IndexManagementImpl;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class IndexStatsCollector implements Callable<TabularData>, IdentifiedDataSerializable {
	
	private IndexManagementImpl idxManager;
	
	public IndexStatsCollector() {
		//
	}
	
	@Autowired
	public void setIdxManager(IndexManagementImpl idxManager) {
		this.idxManager = idxManager;
	}

	@Override
	public TabularData call() throws Exception {
		return idxManager.getIndexStats();
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_CollectIndexStaticsTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		//pathId = in.readInt();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		//out.writeInt(pathId);
	}
	
}
