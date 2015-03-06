package com.bagri.common.stats.watch;


public class MillisTicker implements Ticker {

	@Override
	public long getCurrentTicks() {
		return System.currentTimeMillis();
	}

}
