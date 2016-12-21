package com.bagri.support.stats.watch;


/**
 * Another ticker implementation, uses System.nanoTime API 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class NanosTicker implements Ticker {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCurrentTicks() {
		return System.nanoTime();
	}

}
