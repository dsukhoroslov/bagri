package com.bagri.common.stats;

public class SimpleStopWatch implements StopWatch {
	
	private Ticker ticker;
	private long start;
	private long elapsed;
	
	public SimpleStopWatch(Ticker ticker) {
		this.ticker = ticker;
	}

	@Override
	public void start() {
		start = ticker.getCurrentTicks();
		elapsed = 0;
	}

	@Override
	public long stop() {
		elapsed += ticker.getCurrentTicks() - start;
		start = 0;
		return elapsed;
	}

	@Override
	public long suspend() {
		elapsed += ticker.getCurrentTicks() - start;
		return elapsed;
	}
	
	@Override
	public void resume() {
		start = ticker.getCurrentTicks();
	}

}
