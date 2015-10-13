package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.bagri.common.query.QueriedPath;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class QueriedPathSerializer implements StreamSerializer<QueriedPath> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_QueriedPath;
	}

	@Override
	public QueriedPath read(ObjectDataInput in) throws IOException {
		int dataType = in.readInt();
		boolean indexed = in.readBoolean();
		int[] pids = in.readIntArray();
		List<Integer> paths = new ArrayList<>(pids.length);
		for (int pid: pids) {
			paths.add(pid);
		}
		return new QueriedPath(dataType, indexed, paths);
	}

	@Override
	public void write(ObjectDataOutput out, QueriedPath path) throws IOException {
		out.writeInt(path.getDataType());
		out.writeBoolean(path.isIndexed());
		Collection<Integer> paths = path.getPathIds();
		Iterator<Integer> itr = paths.iterator();
		int[] pids = new int[paths.size()];
		for (int i=0; i < paths.size(); i++) {
			pids[i] = itr.next();
		}
		out.writeIntArray(pids);
	}

}
