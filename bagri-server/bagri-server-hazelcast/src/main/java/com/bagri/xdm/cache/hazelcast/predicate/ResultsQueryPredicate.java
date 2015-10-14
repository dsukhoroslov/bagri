package com.bagri.xdm.cache.hazelcast.predicate;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ResultsQueryPredicate;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;
import static com.bagri.common.util.CollectionUtils.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.bagri.xdm.domain.XDMResults;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicate;

public class ResultsQueryPredicate implements Predicate<Long, XDMResults>, IdentifiedDataSerializable { 
	
	//private static final transient Logger logger = LoggerFactory.getLogger(ResultsDocPredicate.class);
	
	private Set<Integer> queryIds = new HashSet<>();
	
	public ResultsQueryPredicate() {
		// for de-serialization
	}
	
	public ResultsQueryPredicate(Collection<Integer> queryIds) {
		this.queryIds.addAll(queryIds);
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
	@Override
	public int getId() {
		return cli_ResultsQueryPredicate;
	}

	@Override
	public boolean apply(Entry<Long, XDMResults> resEntry) {
		Integer queryKey = resEntry.getKey().intValue();
		return queryIds.contains(queryKey);
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		queryIds.addAll(toIntList(in.readIntArray()));
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeIntArray(toIntArray(queryIds));
	}

}

