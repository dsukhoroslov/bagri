package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.system.Function;
import com.bagri.xdm.system.Parameter;
import com.bagri.xdm.system.DataType;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class FunctionSerializer implements StreamSerializer<Function> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMFunction;
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
			String atn = in.readUTF();
			String val = in.readUTF();
			xFunc.getAnnotations().setProperty(atn, val);
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
		for (String atn: xFunc.getAnnotations().stringPropertyNames()) {
			out.writeUTF(atn);
			out.writeUTF(xFunc.getAnnotations().getProperty(atn));
		}
	}

}
