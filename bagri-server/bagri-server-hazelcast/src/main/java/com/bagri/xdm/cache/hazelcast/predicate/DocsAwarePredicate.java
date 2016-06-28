package com.bagri.xdm.cache.hazelcast.predicate;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_DocsAwarePredicate;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import com.bagri.common.util.CollectionUtils;
import com.bagri.xdm.common.DataKey;
import com.bagri.xdm.domain.Elements;
import com.bagri.xdm.query.PathExpression;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

@SuppressWarnings("serial")
public class DocsAwarePredicate extends QueryPredicate {
	
	private Set<Long> docIds = new HashSet<>();
	
	public DocsAwarePredicate() {
		super();
	}

	public DocsAwarePredicate(PathExpression pex, Object value, Set<Long> docIds) {
		super(pex, value);
		this.docIds.addAll(docIds);
	}

	@Override
	public int getId() {
		return cli_DocsAwarePredicate;
	}

	@Override
	public boolean apply(Entry<DataKey, Elements> xdmEntry) {
		if (docIds.contains(xdmEntry.getKey().getDocumentKey())) {
			return super.apply(xdmEntry);
		}
		return false;
	}	
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		CollectionUtils.fromLongArray(in.readLongArray(), docIds);
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeLongArray(CollectionUtils.toLongArray(docIds));
	}
	
}
