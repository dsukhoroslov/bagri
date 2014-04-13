package com.bagri.common.idgen;

import java.util.concurrent.atomic.AtomicLong;

public class SimpleIdGenerator implements IdGenerator<Long> {

	private final AtomicLong id;
	
	public SimpleIdGenerator(long start) {
		id = new AtomicLong(start);
	}
	
	@Override
	public Long next() {
		return id.incrementAndGet();
	}

	@Override
	public Long[] nextRange(int size) {
		long end = id.addAndGet(size);
		return new Long[] {end - size + 1, end};
	}

}

