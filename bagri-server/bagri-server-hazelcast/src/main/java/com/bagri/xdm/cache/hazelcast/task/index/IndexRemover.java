package com.bagri.xdm.cache.hazelcast.task.index;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_RemoveIndexTask;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.system.XDMIndex;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class IndexRemover implements Callable<Boolean>, IdentifiedDataSerializable { 
	
	private static final transient Logger logger = LoggerFactory.getLogger(IndexRemover.class);
	
	private String index;
	private transient RepositoryImpl xdmRepo;
    
	public IndexRemover() {
		//
	}
	
	public IndexRemover(String index) {
		this.index = index;
	}

    @Autowired
	public void setXDMRepository(RepositoryImpl xdmRepo) {
		this.xdmRepo = xdmRepo;
	}
	
	@Override
	public Boolean call() throws Exception {
		logger.trace("call.enter");
		long stamp = System.currentTimeMillis();
		boolean result = xdmRepo.dropSchemaIndex(index);
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("call.exit; returning: {}; time taken: {}", result, stamp);
		return result;
	}
    
	@Override
	public int getId() {
		return cli_RemoveIndexTask;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		index = in.readUTF();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(index);
	}


}
