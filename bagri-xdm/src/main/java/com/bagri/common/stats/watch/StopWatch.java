package com.bagri.common.stats.watch;

/**
 * 
 * @author Denis Sukhoroslov
 * @since 03/03/2015
 */
public interface StopWatch {
	
	/**
	 * method start
	 */
	void start();

	/**
	 * method stop
	 * @return elapsed time between initial start and stop (now)
	 */
	long stop();
	
	/**
	 * method suspend
	 * @return elapsed time between initial start and suspend (now)
	 */
	long suspend();
	
	/**
	 * method resume
	 */
	void resume();
	
}
