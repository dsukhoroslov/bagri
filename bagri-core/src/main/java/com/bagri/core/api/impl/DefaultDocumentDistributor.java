package com.bagri.core.api.impl;

import com.bagri.core.api.DocumentDistributionStrategy;

public class DefaultDocumentDistributor implements DocumentDistributionStrategy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int getDistributionHash(String uri) {
		return uri.hashCode();
	}

}
