package com.bagri.server.hazelcast.predicate;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_QueryPredicate;
import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.DataKey;
import com.bagri.core.model.Elements;
import com.bagri.core.query.PathExpression;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicate;

public class QueryPredicate implements Predicate<DataKey, Elements>, IdentifiedDataSerializable { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2796783159287994964L;

	private static final transient Logger logger = LoggerFactory.getLogger(QueryPredicate.class);
	
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
		return cli_QueryPredicate;
	}

	@Override
	public boolean apply(Entry<DataKey, Elements> xdmEntry) {
		//logger.trace("apply.enter");
		boolean result = xdmEntry.getValue().apply(pex, value);
		//logger.trace("apply.exit; returning {}", result);
		return result;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		pex = in.readObject();
		value = in.readObject();
		//logger.trace("readData.exit");
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(pex);
		out.writeObject(value);
		//logger.trace("writeData.exit");
	}

}
