package com.bagri.client.hazelcast;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_GroupCountPredicate;
import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.DataKey;
import com.bagri.core.model.Element;
import com.bagri.core.model.Elements;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicate;

@SuppressWarnings("serial")
public class GroupCountPredicate implements Predicate<DataKey, Elements>, IdentifiedDataSerializable {

	private static final transient Logger logger = LoggerFactory.getLogger(GroupCountPredicate.class);
	
	private ConcurrentHashMap<Object, AtomicInteger> aggregates = new ConcurrentHashMap<>(); 
	
	public GroupCountPredicate() {
		// for de-serialization
	}
	
	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
	@Override
	public int getId() {
		return cli_GroupCountPredicate;
	}

	@Override
	public boolean apply(Entry<DataKey, Elements> xEntry) {
		//logger.trace("apply.enter");
		Collection<Element> elts = xEntry.getValue().getElements();
		for (Element elt: elts) {
			AtomicInteger count = aggregates.putIfAbsent(elt.getValue(), new AtomicInteger(1));
			if (count != null) {
				synchronized (count) { 
					count.incrementAndGet();
					aggregates.put(elt.getValue(), count);
				}
			}
		}
		logger.trace("apply; this: {}; aggregates: {}", this, aggregates);
		return false;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		//pex = in.readObject();
		//value = in.readObject();
		logger.trace("readData.exit");
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		//out.writeObject(pex);
		//out.writeObject(value);
		logger.trace("writeData.exit");
	}
	
}
