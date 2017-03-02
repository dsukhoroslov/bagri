package com.bagri.core.server.api.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.DataKey;
import com.bagri.core.model.Data;
import com.bagri.core.model.Elements;
import com.bagri.core.server.api.ModelManagement;

public abstract class ContentBuilderBase {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected ModelManagement model;
	
	/**
	 * 
	 * @param model the model management component. Used to search/add model paths.
	 */
	protected ContentBuilderBase(ModelManagement model) {
		this.model = model;
	}
	
	
	public static Map<DataKey, Elements> dataToElements(List<Data> data) {
		Map<DataKey, Elements> result = new HashMap<>();
		//
		return result;
	}
	
}
