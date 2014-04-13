package com.bagri.xdm.access.hazelcast.process;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_TemplateResultTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.factoryId;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;

public class DocumentBuilder implements Callable<Collection<String>>, Portable {

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
	public int getClassId() {
		return cli_TemplateResultTask;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readPortable(PortableReader in) throws IOException {
		docType = in.readInt("docType");
		template = in.readUTF("template");
		long[] la = in.readLongArray("docIds");
		for (long l : la) {
			docIds.add(l);
		}
		Map<String, String> map = in.getRawDataInput().readObject();
		params.putAll(map);
	}

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		out.writeInt("docType", docType);
		out.writeUTF("template", template);
		int i = 0;
		long[] la = new long[docIds.size()];
		for (long l : docIds) {
			la[i] = l;
			i++;
		}
		out.writeLongArray("docIds", la);
		out.getRawDataOutput().writeObject(params);
	}

}
