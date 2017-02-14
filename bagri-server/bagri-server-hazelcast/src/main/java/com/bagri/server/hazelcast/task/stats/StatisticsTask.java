package com.bagri.server.hazelcast.task.stats;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.server.hazelcast.util.SpringContextHolder.*;

import java.io.IOException;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.bagri.support.stats.StatisticsProvider;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public abstract class StatisticsTask implements IdentifiedDataSerializable {

	//protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private String schemaName;
	private String statsName;
    
	public StatisticsTask() {
		// de-serialize
	}
	
	public StatisticsTask(String schemaName, String statsName) {
		this.schemaName = schemaName;
		this.statsName = statsName;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

    //@Autowired
    //@Qualifier("docStats")
	//public void setXdmStats(InvocationStatistics xdmStats) {
	//	this.xdmStats = xdmStats;
	//	logger.trace("setXdmStats; got statistics: {}", xdmStats); 
	//}
	
	protected StatisticsProvider getStats() {
		ApplicationContext ctx = getContext(schemaName);
		StatisticsProvider stats = ctx.getBean(statsName, StatisticsProvider.class); 
		//logger.trace("getStats; returning: {}, for name: {}", stats, statsName);
		return stats;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		schemaName = in.readUTF();
		statsName = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(schemaName);
		out.writeUTF(statsName);
	}

}
