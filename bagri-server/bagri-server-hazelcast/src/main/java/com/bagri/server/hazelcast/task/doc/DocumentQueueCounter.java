package com.bagri.server.hazelcast.task.doc;

import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_CountUpdatingDocumentsTask;

import java.io.IOException;
import java.util.concurrent.Callable;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.server.hazelcast.impl.PopulationManagementImpl;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentQueueCounter implements Callable<Integer>, IdentifiedDataSerializable { 
	
	//private static final transient Logger logger = LoggerFactory.getLogger(DocumentQueueCounter.class);

	private transient SchemaRepositoryImpl xdmRepo;
    
    @Autowired
	public void setXDMRepository(SchemaRepositoryImpl xdmRepo) {
		this.xdmRepo = xdmRepo;
	}

	@Override
	public Integer call() throws Exception {
		return ((PopulationManagementImpl) xdmRepo.getPopulationManagement()).getUpdatingDocumentCount();
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_CountUpdatingDocumentsTask;
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
