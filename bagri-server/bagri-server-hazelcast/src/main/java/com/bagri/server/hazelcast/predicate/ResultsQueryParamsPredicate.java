package com.bagri.server.hazelcast.predicate;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_ResultsQueryParamsPredicate;
import static com.bagri.support.util.XQUtils.adjustSearchValue;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.core.model.Elements;
import com.bagri.core.model.QueryResult;
import com.bagri.core.query.PathExpression;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicate;

public class ResultsQueryParamsPredicate implements Predicate<Long, QueryResult>, IdentifiedDataSerializable {   
	
	private static final long serialVersionUID = 1L;
	
	private int queryId;
	private PathExpression pex;
	private Elements elts;
	
	public ResultsQueryParamsPredicate() {
		// for de-serialization
	}
	
	public ResultsQueryParamsPredicate(int queryId, PathExpression pex, Elements elts) {
		this.queryId = queryId;
		this.pex = pex;
		this.elts = elts;
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}
	
	@Override
	public int getId() {
		return cli_ResultsQueryParamsPredicate;
	}

	@Override
	public boolean apply(Entry<Long, QueryResult> resEntry) {
		long resultKey = resEntry.getKey();
		int queryKey = (int) (resultKey >> 32);
		if (queryKey == queryId) {
			Object param = resEntry.getValue().getParams().get(pex.getParamName());
			Object value = adjustSearchValue(param, pex.getCachedPath().getDataType());
			if (value != null) {
				return elts.apply(pex, value);
			}
		}
		return false;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		queryId = in.readInt();
		pex = in.readObject();
		elts = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(queryId);
		out.writeObject(pex);
		out.writeObject(elts);
	}

}


