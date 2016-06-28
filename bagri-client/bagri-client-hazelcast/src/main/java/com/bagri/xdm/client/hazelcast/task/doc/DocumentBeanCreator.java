package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_CreateBeanDocumentTask;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.xdm.domain.Document;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentBeanCreator extends DocumentAwareTask implements Callable<Document> {
	
	protected Object bean;

	public DocumentBeanCreator() {
		super();
	}

	public DocumentBeanCreator(String clientId, long txId, String uri, Properties props, Object bean) {
		super(clientId, txId, uri, props);
		this.bean = bean;
	}

	@Override
	public Document call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_CreateBeanDocumentTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		bean = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeObject(bean);
	}

}
