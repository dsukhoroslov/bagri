package com.bagri.common.stats.watch;


public class NanosTicker implements Ticker {

	@Override
	public long getCurrentTicks() {
		return System.nanoTime();
	}

}
