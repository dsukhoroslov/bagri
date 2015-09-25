package com.bagri.xdm.client.hazelcast.impl;

import com.hazelcast.core.IdGenerator;

public class HzGeneratorImpl implements com.bagri.common.idgen.IdGenerator<Long> {
	
	private IdGenerator idGen; 

	public HzGeneratorImpl(IdGenerator idGen) {
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
		return result; 
	}

}
