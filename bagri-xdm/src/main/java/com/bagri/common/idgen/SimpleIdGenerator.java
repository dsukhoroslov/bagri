package com.bagri.common.idgen;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleIdGenerator implements IdGenerator<Long> {

	private final AtomicLong id;
	
	public SimpleIdGenerator() {
		this(0);
	}
	
	public SimpleIdGenerator(long start) {
		id = new AtomicLong(start);
	}
	
	public SimpleIdGenerator(AtomicLong id) {
		this.id = id;
	}
	
	@Override
	public boolean adjust(Long newValue) {
		synchronized (id) {
			if (id.get() < newValue) {
				id.set(newValue);
				return true;
			}
		}
		return false;
	}

	@Override
	public Long next() {
		return id.incrementAndGet();
	}

	@Override
	public Long[] nextRange(int size) {
		Long[] result = new Long[size];
		long current = id.getAndAdd(size);
		for (int i=0; i < size; i++) {
			result[i] = ++current;
		}
		return result; 
	}


}

