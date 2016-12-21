package com.bagri.support.stats.watch;

/**
 * A simple ticker implementation, uses System.currentTimeMillis API 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class MillisTicker implements Ticker {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getCurrentTicks() {
		return System.currentTimeMillis();
	}

}
