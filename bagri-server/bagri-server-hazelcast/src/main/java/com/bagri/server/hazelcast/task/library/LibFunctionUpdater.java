package com.bagri.server.hazelcast.task.library;

import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_UpdateLibraryTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.bagri.core.system.Cardinality;
import com.bagri.core.system.DataType;
import com.bagri.core.system.Function;
import com.bagri.core.system.Library;
import com.bagri.core.system.Parameter;
import com.bagri.support.util.ReflectUtils;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class LibFunctionUpdater extends LibraryProcessor implements IdentifiedDataSerializable {

	private Action action;
	private String className;
	private String prefix;
	private String description;
	private String signature;
	
	public LibFunctionUpdater() {
		// de-ser
	}
	
	
	public LibFunctionUpdater(int version, String admin, String className, String prefix, String description, String signature, Action action) {
		super(version, admin);
		this.className = className;
		this.prefix = prefix;
		this.description = description;
		this.signature = signature;
		this.action = action;
	}

	@Override
	public Object process(Entry<String, Library> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			Function xdf = buildFunction();
			if (xdf != null) {
				Library xdl = entry.getValue();
				if (action == Action.remove) {
					//
					xdl.getFunctions().remove(xdf);
				} else {
					xdl.getFunctions().add(xdf);
				}
				xdl.updateVersion(getAdmin());
				entry.setValue(xdl);
				auditEntity(AuditType.update, xdl);
				return xdf;
			}
		} 
		return null;
	}
	
	private Function buildFunction()  {
		// TODO: do it via regexp ?
		try {
			Class cls = Class.forName(className);
			int idx = signature.indexOf("(");
			if (idx > 0) {
				String method = signature.substring(0, idx);
				method = method.trim();
				String args = signature.substring(idx + 1, signature.indexOf(")"));
				idx = signature.lastIndexOf(":");
				String result = signature.substring(idx + 1);
				result = result.trim();
				ReflectUtils.type2Class(result);
				// parse cardinality properly..
				DataType resultType = new DataType(result, Cardinality.one); 
				Function xdf = new Function(className, method, resultType, description, prefix);
				StringTokenizer st = new StringTokenizer(args.trim(), " ,");
				List<Class> clsa = new ArrayList<>();
				while (st.hasMoreTokens()) {
					String name = st.nextToken();
					String type = st.nextToken();
					clsa.add(ReflectUtils.type2Class(type));
					// parse cardinality properly..
					Parameter xdp = new Parameter(name, type, Cardinality.one);
					xdf.getParameters().add(xdp);
				}
				cls.getMethod(method, clsa.toArray(new Class[clsa.size()]));
				return xdf;
			}
		} catch (Exception ex) {
			logger.warn("buildFunction.error; can't parse signature: {}.{}; error: {}", className, signature, ex.getMessage()); 
		}
		return null;
	}
	
	@Override
	public int getId() {
		return cli_UpdateLibraryTask;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		className = in.readUTF();
		prefix = in.readUTF();
		description = in.readUTF();
		signature = in.readUTF();
		action = Action.valueOf(in.readUTF());
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(className);
		out.writeUTF(prefix);
		out.writeUTF(description);
		out.writeUTF(signature);
		out.writeUTF(action.name());
	}

	
}
