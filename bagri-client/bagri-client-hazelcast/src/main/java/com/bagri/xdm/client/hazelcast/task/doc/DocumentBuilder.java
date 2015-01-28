package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_TemplateResultTask;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

// this task class is never used! do we need it at all?

public class DocumentBuilder implements Callable<Collection<String>>, IdentifiedDataSerializable {

	protected int docType;
	protected String template;
	protected Set<Long> docIds = new HashSet<Long>();
	protected Map<String, String> params = new HashMap<String, String>();

	public DocumentBuilder() {
		//
	}

	public DocumentBuilder(int docType, String template, Collection<Long> docIds, Map<String, String> params) {
		this.docType = docType;
		this.template = template;
		if (docIds != null) {
			this.docIds.addAll(docIds);
		}
		if (params != null) {
			this.params.putAll(params);
		}
	}

	@Override
	public Collection<String> call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_TemplateResultTask;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		docType = in.readInt();
		template = in.readUTF();
		Set<Long> ids = in.readObject();
		docIds.addAll(ids);
		Map<String, String> map = in.readObject();
		params.putAll(map);
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(docType);
		out.writeUTF(template);
		out.writeObject(docIds);
		out.writeObject(params);
	}

}
