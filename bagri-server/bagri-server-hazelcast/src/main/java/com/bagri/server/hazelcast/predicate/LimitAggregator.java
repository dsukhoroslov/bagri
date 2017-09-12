package com.bagri.server.hazelcast.predicate;

import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_LimitAggregator;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import com.hazelcast.aggregation.Aggregator;
import com.hazelcast.aggregation.impl.AbstractAggregator;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.IndexAwarePredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.QueryContext;
import com.hazelcast.query.impl.QueryableEntry;

public class LimitAggregator<K, I, V> extends AbstractAggregator<I, V, Collection<V>> implements IndexAwarePredicate<K, V>, IdentifiedDataSerializable {

	private static final transient Logger logger = LoggerFactory.getLogger(LimitAggregator.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int found;
	private int limit;
	private Predicate<K, V> predicate;
	private List<V> result = new ArrayList<>();
	
	public LimitAggregator() {
		//
	}
	
	public LimitAggregator(int limit, Predicate<K, V> predicate)  {
		this.limit = limit;
		this.predicate = predicate;
	}

	//@Override
	protected void accumulateExtracted(I entry, V value) {
		accumulateExtracted(value);
	}

	@Override
	protected void accumulateExtracted(V value) {
		logger.info("accumulateExtracted.enter; value: {}; }", value); 
		if (found < limit) {
			result.add(value);
			found++;
		}
		logger.info("accumulateExtracted.exit; found: {}; result: {}", found, result); 
	}

	@Override
	public void combine(Aggregator aggregator) {
		logger.info("combine.enter; aggregator: {}; this: {}", aggregator, this);
		if (aggregator instanceof LimitAggregator) {
			result.addAll(((LimitAggregator<K, I, V>) aggregator).result);
			found += ((LimitAggregator<K, I, V>) aggregator).result.size();
		}
		logger.info("combine.exit; found: {}; result {}", found, result);
	}

	@Override
	public Collection<V> aggregate() {
		logger.info("aggregate; result: {}", result);
		if (result.size() > limit) {
			List<V> r = new ArrayList<>(limit);
			for (int i=0; i < limit; i++) {
				r.add(result.get(i));
			}
			result = r;
		}
		return result;
	}

	@Override
    public void onAccumulationFinished() {
		logger.info("onAccumulationFinished; found: {}; result: {}", found, result); 
    }

	@Override
    public void onCombinationFinished() {
		logger.info("onCombinationFinished; found: {}; result: {}", found, result); 
    }
    
	@Override
	public boolean apply(Entry<K, V> entry) {
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
		return cli_LimitAggregator;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		limit = in.readInt();
		predicate = in.readObject();
		//int size = in.readInt();
		//((ArrayList<V>) result).ensureCapacity(size);
		//for (int i=0; i < size; i++) {
		//	result.add((V) in.readObject());
		//}
		//logger.info("readData; this: {}", this); 
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(limit);
		out.writeObject(predicate);
		//out.writeInt(result.size());
		//for (V v: result) {
		//	out.writeObject(v);
		//}
		//logger.info("writeData; this: {}", this); 
	}

}
