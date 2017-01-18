package com.bagri.client.hazelcast.impl;

import com.bagri.support.idgen.IdGenerator;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IFunction;

public class IdGeneratorImpl implements IdGenerator<Long> {
	
	private IAtomicLong idGen; 

	public IdGeneratorImpl(IAtomicLong idGen) {
		this.idGen = idGen;
	}
	
	@Override
	public boolean adjust(Long newValue) {
		return newValue == idGen.alterAndGet(new MaxValue(newValue));
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
	
	
	public static class MaxValue implements IFunction<Long, Long> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4889277331209519782L;
		
		private long newValue;
		
		MaxValue(long value) {
			this.newValue = value;
		}

		@Override
		public Long apply(Long input) {
			if (input < newValue) {
				//input = newValue;
				return newValue;
			}
			return input;
		}
		
	}


}
