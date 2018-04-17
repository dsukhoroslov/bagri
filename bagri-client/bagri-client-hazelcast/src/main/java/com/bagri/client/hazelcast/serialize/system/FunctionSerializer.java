package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bagri.client.hazelcast.serialize.DomainSerializationFactory;
import com.bagri.core.system.DataType;
import com.bagri.core.system.Function;
import com.bagri.core.system.Parameter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class FunctionSerializer implements StreamSerializer<Function> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DomainSerializationFactory.cli_XDMFunction;
	}

	@Override
	public Function read(ObjectDataInput in) throws IOException {
		Function xFunc = new Function(
				in.readUTF(),
				in.readUTF(),
				(DataType) in.readObject(),
				in.readUTF(), 
				in.readUTF());
		int cnt = in.readInt();
		for (int i=0; i < cnt; i++) {
			Parameter xp = in.readObject();
			xFunc.getParameters().add(xp);
		}
		cnt = in.readInt();
		for (int i=0; i < cnt; i++) {
			String name = in.readUTF();
			int sz1 = in.readInt();
			for (int j=0; j < sz1; j++) {
				int sz2 = in.readInt();
				List<String> values = new ArrayList<>(sz2);
				for (int k=0; k < sz2; k++) {
					values.add(in.readUTF());
				}
				xFunc.addAnnotation(name, values);
			}
		}
		return xFunc;
	}

	@Override
	public void write(ObjectDataOutput out, Function xFunc) throws IOException {
		out.writeUTF(xFunc.getClassName());
		out.writeUTF(xFunc.getMethod());
		out.writeObject(xFunc.getResult());
		out.writeUTF(xFunc.getDescription());
		out.writeUTF(xFunc.getPrefix());
		out.writeInt(xFunc.getParameters().size());
		for (Parameter xp: xFunc.getParameters()) {
			out.writeObject(xp);
		}
		out.writeInt(xFunc.getAnnotations().size());
		for (Map.Entry<String, List<List<String>>> entry: xFunc.getAnnotations().entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeInt(entry.getValue().size());
			for (List<String> values: entry.getValue()) {
				out.writeInt(values.size());
				for (String value: values) {
					out.writeUTF(value);
				}
			}
		}
	}

}
