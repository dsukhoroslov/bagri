package com.bagri.xdm.process.hazelcast;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.factoryId;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.common.manage.InvocationStatistics;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public abstract class DocumentStatsTask implements IdentifiedDataSerializable {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	protected transient InvocationStatistics xdmStats;
    
	public DocumentStatsTask() {
		// de-serialize
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

    @Autowired
	public void setXdmStats(InvocationStatistics xdmStats) {
		this.xdmStats = xdmStats;
		logger.trace("setXdmStats; got statistics: {}", xdmStats); 
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		//
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		//
	}

}
