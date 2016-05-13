package com.bagri.xdm.cache.hazelcast.task.model;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_RegisterModelTask;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.client.common.impl.ModelManagementBase;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class ModelRegistrator implements Callable<Integer>, IdentifiedDataSerializable {
	
	private String schemaFile;
	private XDMModelManagement modelMgr;
	
	public ModelRegistrator() {
		//
	}
	
	public ModelRegistrator(String schemaFile) {
		this.schemaFile = schemaFile;
	}

    @Autowired
	public void setModelManagement(XDMModelManagement modelMgr) {
		this.modelMgr = modelMgr;
	}

	@Override
	public Integer call() throws Exception {
		int size = ((ModelManagementBase) modelMgr).getDocumentTypes().size();
		//Path path = Paths.get(schemaFile, null);
		//if (Files.isDirectory(path, null)) {
			modelMgr.registerSchemaUri(schemaFile);			
		//} else {
		//	modelMgr.registerSchemaUri(schemaFile);
		//}
		return ((ModelManagementBase) modelMgr).getDocumentTypes().size() - size;
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

