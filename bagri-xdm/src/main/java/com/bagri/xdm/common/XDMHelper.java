package com.bagri.xdm.common;

public class XDMHelper {
	
	private static XDMFactory _factory = null;
	
	public static synchronized XDMFactory getXDMFactory() {
		return _factory;
	}
	
	public static synchronized void registerXDMFactory(XDMFactory factory) {
		if (_factory == null) {
			_factory = factory;
		} else {
			throw new IllegalStateException("XDM Factory already registered: " + _factory.toString());
		}
	}

}
