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
	protected Set<String> uris = new HashSet<String>();
	protected Map<String, String> params = new HashMap<String, String>();

	public DocumentBuilder() {
		//
	}

	public DocumentBuilder(int docType, String template, Collection<String> uris, Map<String, String> params) {
		this.docType = docType;
		this.template = template;
		if (uris != null) {
			this.uris.addAll(uris);
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
		int size = in.readInt("size");
		for (int i=0; i < size; i++) {
			uris.add(in.readUTF("uri" + i));
		}
		Map<String, String> map = in.getRawDataInput().readObject();
		params.putAll(map);
	}

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		out.writeInt("docType", docType);
		out.writeUTF("template", template);
		out.writeInt("size", uris.size());
		int i = 0;
		for (String uri: uris) {
			out.writeUTF("uri" + i, uri);
			i++;
		}
		//out.getRawDataOutput().writeObject(uris);
		out.getRawDataOutput().writeObject(params);
	}

}
