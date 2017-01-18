package com.bagri.support.stats.watch;

/**
 * A simple single-threaded StopWatch implementation
 * 
 * @author Denis Sukhoroslov
 *
 */
public class SimpleStopWatch implements StopWatch {
	
	private Ticker ticker;
	private long start;
	private long elapsed;
	
	/**
	 * 
	 * @param ticker the Ticker to measure ticks
	 */
	public SimpleStopWatch(Ticker ticker) {
		this.ticker = ticker;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() {
		start = ticker.getCurrentTicks();
		elapsed = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long stop() {
		elapsed += ticker.getCurrentTicks() - start;
		start = 0;
		return elapsed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long suspend() {
		elapsed += ticker.getCurrentTicks() - start;
		return elapsed;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resume() {
		start = ticker.getCurrentTicks();
	}

}
