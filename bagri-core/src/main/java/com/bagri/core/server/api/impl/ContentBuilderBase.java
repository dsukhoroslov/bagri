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
		long docKey = 0;
		Map<DataKey, Elements> result = new HashMap<>();
		for (Data xdm: data) {
			if (xdm.getValue() != null) {
				DataKey xdk = new DataKey(docKey, xdm.getPathId());
				Elements xdes = result.get(xdk);
				if (xdes == null) {
					xdes = new Elements(xdk.getPathId(), null);
					result.put(xdk, xdes);
				}
				xdes.addElement(xdm.getElement());
			}
		}
		return result;
	}
	
}
