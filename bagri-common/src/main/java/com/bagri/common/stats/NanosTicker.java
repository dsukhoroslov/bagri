package com.bagri.common.stats;

public class NanosTicker implements Ticker {

	@Override
	public long getCurrentTicks() {
		return System.nanoTime();
	}

}
