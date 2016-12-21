package com.bagri.support.idgen;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple not-clustered implementation of IdGenerator.  
 *  
 * @author Denis Sukhoroslov
 *
 */
public class SimpleIdGenerator implements IdGenerator<Long> {

	private final AtomicLong id;
	
	/**
	 * default constructor
	 */
	public SimpleIdGenerator() {
		this(0);
	}
	
	/**
	 * 
	 * @param start the value to start with
	 */
	public SimpleIdGenerator(long start) {
		id = new AtomicLong(start);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long next() {
		return id.incrementAndGet();
	}

	/**
	 * {@inheritDoc}
	 */
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

