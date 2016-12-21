package com.bagri.support.stats.watch;

/**
 * Represents a StopWatch to measure internal operations duration
 * 
 * @author Denis Sukhoroslov
 * @since 03/03/2015
 */
public interface StopWatch {
	
	/**
	 * Starts the StopWatch
	 */
	void start();

	/**
	 * Stops the StopWatch
	 * 
	 * @return elapsed time between initial start and stop (now)
	 */
	long stop();
	
	/**
	 * Suspends the StopWatch
	 * 
	 * @return elapsed time between initial start and suspend (now)
	 */
	long suspend();
	
	/**
	 * Resumes the StopWatch
	 */
	void resume();
	
}
