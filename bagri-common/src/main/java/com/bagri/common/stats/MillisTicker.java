package com.bagri.common.stats;

public class MillisTicker implements Ticker {

	@Override
	public long getCurrentTicks() {
		return System.currentTimeMillis();
	}

}
