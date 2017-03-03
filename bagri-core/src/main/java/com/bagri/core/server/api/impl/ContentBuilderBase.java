package com.bagri.core.server.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.DataKey;
import com.bagri.core.model.Data;
import com.bagri.core.model.Element;
import com.bagri.core.model.Elements;
import com.bagri.core.model.Path;
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
	
    protected Collection<Data> buildDataList(Map<DataKey, Elements> elements) {

    	List<Data> dataList = new ArrayList<>(elements.size() * 2);
    	// here the source elements contain elements with values only
    	// we should enrich the collection with intermediate parents
    	for (Map.Entry<DataKey, Elements> entry: elements.entrySet()) {
    		int pathId = entry.getKey().getPathId();
    		Path path = model.getPath(pathId);
    		if (path == null) {
        		logger.info("buildDataSet; can't get path for pathId: {}", pathId);
        		continue;
    		}
    		
    		Elements elts = entry.getValue();
    		for (Element element: elts.getElements()) {
    			Data data = new Data(path, element);
    			dataList.add(data);
    		}
    	}
    	Collections.sort(dataList);
    	return dataList;
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
