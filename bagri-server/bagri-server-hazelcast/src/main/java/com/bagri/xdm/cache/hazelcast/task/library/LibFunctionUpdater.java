package com.bagri.xdm.cache.hazelcast.task.library;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_UpdateLibraryTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import com.bagri.common.util.ReflectUtils;
import com.bagri.xdm.system.XDMCardinality;
import com.bagri.xdm.system.XDMFunction;
import com.bagri.xdm.system.XDMLibrary;
import com.bagri.xdm.system.XDMParameter;
import com.bagri.xdm.system.XDMType;
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
	public Object process(Entry<String, XDMLibrary> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMFunction xdf = buildFunction();
			if (xdf != null) {
				XDMLibrary xdl = entry.getValue();
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
	
	private XDMFunction buildFunction()  {
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
				XDMType resultType = new XDMType(result, XDMCardinality.one); 
				XDMFunction xdf = new XDMFunction(className, method, resultType, description, prefix);
				StringTokenizer st = new StringTokenizer(args.trim(), " ,");
				List<Class> clsa = new ArrayList<>();
				while (st.hasMoreTokens()) {
					String name = st.nextToken();
					String type = st.nextToken();
					clsa.add(ReflectUtils.type2Class(type));
					// parse cardinality properly..
					XDMParameter xdp = new XDMParameter(name, type, XDMCardinality.one);
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
