package com.bagri.server.hazelcast.task.model;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_RegisterModelTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ModelManagementBase;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class ModelRegistrator implements Callable<Integer>, IdentifiedDataSerializable {
	
	private String schemaFile;
	private ModelManagement modelMgr;
	
	public ModelRegistrator() {
		//
	}
	
	public ModelRegistrator(String schemaFile) {
		this.schemaFile = schemaFile;
	}

    @Autowired
	public void setModelManagement(ModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}

	@Override
	public Integer call() throws Exception {
		//int size = ((ModelManagementBase) modelMgr).getDocumentTypes().size();
		//Path path = Paths.get(schemaFile, null);
		//if (Files.isDirectory(path, null)) {
			modelMgr.registerSchemaUri(schemaFile);			
		//} else {
		//	modelMgr.registerSchemaUri(schemaFile);
		//}
		//return ((ModelManagementBase) modelMgr).getDocumentTypes().size() - size;
		return 1;
	}

	@Override
	public int getId() {
		return cli_RegisterModelTask;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		schemaFile = in.readUTF();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(schemaFile);
	}

}

