package com.bagri.core.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.DocumentDistributionStrategy;

public class FirstDotDistributor implements DocumentDistributionStrategy {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(FirstDotDistributor.class);
	
	@Override
	public int getDistributionHash(String uri) {
		int pos = uri.indexOf('.');
		if (pos > 0) {
			uri = uri.substring(0, pos);
		}
		int hash = uri.hashCode();
		logger.trace("getDistributionHash; returning {} for uri {}", hash, uri);
		return hash;
	}

}
