package com.bagri.xdm.access.hazelcast.impl;

import com.hazelcast.core.IdGenerator;

public class HazelcastIdGenerator implements com.bagri.common.idgen.IdGenerator<Long> {
	
	private IdGenerator idGen; 

	public HazelcastIdGenerator(IdGenerator idGen) {
		this.idGen = idGen;
	}

	@Override
	public Long next() {
		//
		return idGen.newId();
	}

	@Override
	public Long[] nextRange(int size) {
		Long[] result = new Long[size];
			for (int i=0; i < size; i++) {
			result[i] = idGen.newId();
		}
		//Range r = idGen.next(size);
		return result; //new Long[] {0L, 0L}; //{r.getFrom(), r.getTo()};
	}

}
