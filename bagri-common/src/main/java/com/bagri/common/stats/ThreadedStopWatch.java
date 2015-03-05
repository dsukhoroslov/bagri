package com.bagri.common.stats;

public class ThreadedStopWatch implements StopWatch {

	private Ticker ticker;
	private ThreadLocal<SimpleStopWatch> wdt = new ThreadLocal<SimpleStopWatch>() {
		
		@Override
		protected SimpleStopWatch initialValue() {
			return new SimpleStopWatch(ticker);
		}
		
	};
	
	@Override
	public void start() {
		wdt.get().start();
	}

	@Override
	public long stop() {
		return wdt.get().stop();
	}

	@Override
	public long suspend() {
		return wdt.get().suspend();
	}

	@Override
	public void resume() {
		wdt.get().resume();
	}

	public void setTicker(Ticker ticker) {
		this.ticker = ticker;
	}
		
}
