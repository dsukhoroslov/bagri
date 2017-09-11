package com.bagri.core.api.impl;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.DocumentAccessor;

public abstract class DocumentAccessorBase implements DocumentAccessor {

    protected Logger logger = LoggerFactory.getLogger(getClass());

	protected Object content;

	@Override
	public <T> T getContent() {
		return (T) content;
	}

	@Override
	public Properties getProperties() {
		// for future use
		return null;
	}

	@Override
	public String getProperty(String pName) {
		// for future use
		return null;
	}

}
