package com.bagri.support.stats.watch;

/**
 * Represents unified structure which can keep and count system ticks
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface Ticker {
	
	/**
	 * 
	 * @return the current system ticks
	 */
	long getCurrentTicks();

}
