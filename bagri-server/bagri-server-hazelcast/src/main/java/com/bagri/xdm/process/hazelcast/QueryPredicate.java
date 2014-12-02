package com.bagri.xdm.process.hazelcast;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_ApplyQueryTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.common.query.PathExpression;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMElements;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicate;

public class QueryPredicate implements Predicate<XDMDataKey, XDMElements>, IdentifiedDataSerializable { 
	
	//private static final transient Logger logger = LoggerFactory.getLogger(QueryProcessor.class);
	//private Set<String> threads = new HashSet<String>();
	
	private PathExpression pex;
	private Object value;
	
	public QueryPredicate() {
		// for de-serialization
	}
	
	public QueryPredicate(PathExpression pex, Object value) {
		this.pex = pex;
		this.value = value;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
	@Override
	public int getId() {
		return cli_ApplyQueryTask;
	}

	@Override
	public boolean apply(Entry<XDMDataKey, XDMElements> xdmEntry) {
		return xdmEntry.getValue().apply(pex, value);
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		pex = in.readObject();
		value = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(pex);
		out.writeObject(value);
	}

}
