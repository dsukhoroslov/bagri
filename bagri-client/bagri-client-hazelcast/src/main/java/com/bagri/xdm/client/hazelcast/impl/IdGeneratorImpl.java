package com.bagri.xdm.client.hazelcast.impl;

import com.bagri.common.idgen.IdGenerator;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IFunction;

public class IdGeneratorImpl implements IdGenerator<Long> {
	
	private IAtomicLong idGen; 

	public IdGeneratorImpl(IAtomicLong idGen) {
		this.idGen = idGen;
	}
	
	@Override
	public boolean adjust(final Long newValue) {
		
		long changed = idGen.alterAndGet(new IFunction<Long, Long>() {

			@Override
			public Long apply(Long input) {
				if (input < newValue) {
					input = newValue;
					return newValue;
				}
				return input;
			}
			
		});
		return changed == newValue;
	}

	@Override
	public Long next() {
		//
		return idGen.incrementAndGet();
	}

	@Override
	public Long[] nextRange(int size) {
		Long[] result = new Long[size];
		long current = idGen.getAndAdd(size);
		for (int i=1; i <= size; i++) {
			result[i] = current + 1;
		}
		return result; 
	}

}
