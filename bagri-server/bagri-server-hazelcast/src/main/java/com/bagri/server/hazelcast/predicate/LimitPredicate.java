package com.bagri.server.hazelcast.predicate;

import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_LimitPredicate;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.IndexAwarePredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.QueryContext;
import com.hazelcast.query.impl.QueryableEntry;
import com.hazelcast.query.impl.predicates.AbstractIndexAwarePredicate;
import com.hazelcast.query.impl.predicates.AbstractPredicate;

public class LimitPredicate<K, V> extends AbstractIndexAwarePredicate<K, V> implements IdentifiedDataSerializable {
	
	private static final transient Logger logger = LoggerFactory.getLogger(LimitPredicate.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int found;
	private int limit;
	private Predicate<K, V> predicate;
	
	public LimitPredicate() {
		//
	}
	
	public LimitPredicate(int limit, Predicate<K, V> predicate)  {
		this.limit = limit;
		this.predicate = predicate;
	}

	@Override
	public boolean apply(Entry<K, V> entry) {
		//logger.info("apply; entry: {}", entry);
		if (found < limit) {
			if (predicate.apply(entry)) {
				found++;
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	protected boolean applyForSingleAttributeValue(Entry entry, Comparable attributeValue) {
		logger.info("applyForSingleAttributeValue; entry: {}", entry);
		//if (found < limit) {
		//	if (((AbstractPredicate) predicate).apply(mapEntry)(entry)) {
		//		found++;
		//		return true;
		//	}
		//	return false;
		//}
		return false;
	}

	@Override
	public Set<QueryableEntry<K, V>> filter(QueryContext context) {
		//logger.info("filter; context: {}", context);
		Set<QueryableEntry<K, V>> result = ((IndexAwarePredicate) predicate).filter(context);
		//logger.info("filter; result: {}", result);
		if (result.size() > limit) {
			Set<QueryableEntry<K, V>> head = new HashSet<>(limit);
			for (QueryableEntry<K, V> entry: result) {
				if (found < limit) {
					head.add(entry);
					found++;
				} else {
					break;
				}
			}
			result = head;
		}
		return result;
	}

	@Override
	public boolean isIndexed(QueryContext context) {
		if (predicate instanceof IndexAwarePredicate) {
			return ((IndexAwarePredicate) predicate).isIndexed(context);
		}
		return false;
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_LimitPredicate;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		limit = in.readInt();
		predicate = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(limit);
		out.writeObject(predicate);
	}

}
