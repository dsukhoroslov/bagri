package com.bagri.xdm.client.hazelcast.impl;

import com.hazelcast.core.IdGenerator;

public class HzGeneratorImpl implements com.bagri.common.idgen.IdGenerator<Long> {
	
	private long id;
	private IdGenerator idGen; 

	public HzGeneratorImpl(IdGenerator idGen) {
		this.idGen = idGen;
		id = idGen.newId();
	}

	@Override
	public boolean adjust(Long newValue) {
		synchronized (idGen) {
			if (id < newValue) {
				idGen.init(newValue - 1);
				id = idGen.newId();
				return true;
			}
		}
		return false;
	}

	@Override
	public Long next() {
		long result = id;
		id = idGen.newId();
		return result;
	}

	@Override
	public Long[] nextRange(int size) {
		Long[] result = new Long[size];
		result[0] = id;
		for (int i=1; i < size; i++) {
			id = idGen.newId();
			result[i] = id;
		}
		return result; 
	}


}
