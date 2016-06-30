package com.bagri.common.stats.watch;

/**
 * Another implementation of StopWatch, thread-safe.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class ThreadedStopWatch implements StopWatch {

	private Ticker ticker;
	private ThreadLocal<SimpleStopWatch> wdt = new ThreadLocal<SimpleStopWatch>() {
		
		@Override
		protected SimpleStopWatch initialValue() {
			return new SimpleStopWatch(ticker);
		}
		
	};
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start() {
		wdt.get().start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long stop() {
		return wdt.get().stop();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long suspend() {
		return wdt.get().suspend();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resume() {
		wdt.get().resume();
	}

	/**
	 * Initializes StopWatch with the Ticker provided. Must be used before the first use of the StopWatch.
	 * 
	 * @param ticker the Ticker to use
	 */
	public void setTicker(Ticker ticker) {
		this.ticker = ticker;
	}
		
}
