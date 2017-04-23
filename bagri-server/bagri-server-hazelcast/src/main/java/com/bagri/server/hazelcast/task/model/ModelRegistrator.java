package com.bagri.server.hazelcast.task.model;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_RegisterModelTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.server.api.ContentModeler;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class ModelRegistrator implements Callable<Integer>, IdentifiedDataSerializable {
	
	private String dataFormat;
	private String modelPath;
	private boolean singleModel;
	private transient SchemaRepositoryImpl xdmRepo;
	
	public ModelRegistrator() {
		//
	}
	
	public ModelRegistrator(String dataFormat, String modelPath, boolean singleModel) {
		this.dataFormat = dataFormat; 
		this.modelPath = modelPath;
		this.singleModel = singleModel;
	}

    @Autowired
	public void setXDMRepository(SchemaRepositoryImpl xdmRepo) {
		this.xdmRepo = xdmRepo;
	}
	
	@Override
	public Integer call() throws Exception {
		//int size = ((ModelManagementBase) modelMgr).getDocumentTypes().size();
		//Path path = Paths.get(schemaFile, null);
		//if (Files.isDirectory(path, null)) {

		ContentModeler cm = xdmRepo.getModeler(dataFormat);
		if (singleModel) {
			cm.registerModelUri(modelPath);
		} else {
			cm.registerModelUri(modelPath);
		}
			//modelMgr.registerSchemaUri(schemaFile);			
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
		dataFormat = in.readUTF();
		modelPath = in.readUTF();
		singleModel = in.readBoolean();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(dataFormat);
		out.writeUTF(modelPath);
		out.writeBoolean(singleModel);
	}

}

