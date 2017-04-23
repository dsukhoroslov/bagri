package com.bagri.core.server.api.impl;

import static com.bagri.support.util.FileUtils.def_encoding;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.DataKey;
import com.bagri.core.api.BagriException;
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
	
	/**
	 * {@inheritDoc}
	 */
   	public abstract String buildString(Collection<Data> elements) throws BagriException;
   	
	/**
	 * {@inheritDoc}
	 */
	public String buildString(Map<DataKey, Elements> elements) throws BagriException {
    	Collection<Data> dataList = buildDataList(elements);
    	return buildString(dataList);
	}

	/**
	 * {@inheritDoc}
	 */
	public InputStream buildStream(Map<DataKey, Elements> elements) throws BagriException {
    	Collection<Data> dataList = buildDataList(elements);
    	return buildStream(dataList);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public InputStream buildStream(Collection<Data> elements) throws BagriException {
		String content = buildString(elements);
		if (content != null) {
			try {
				return new ByteArrayInputStream(content.getBytes(def_encoding));
			} catch (UnsupportedEncodingException ex) {
				throw new BagriException(ex, BagriException.ecInOut);
			}
		}
		return null;
	}
	
    protected Collection<Data> buildDataList(Map<DataKey, Elements> elements) {
		logger.trace("buildDataList; got elements: {}", elements);
    	Map<Element, Data> dataMap = new HashMap<>();
    	List<Data> dataList = new ArrayList<>(elements.size()*2);
    	// here the source elements contain elements with values only
    	// we should enrich the collection with intermediate parents
    	for (Map.Entry<DataKey, Elements> entry: elements.entrySet()) {
    		int pathId = entry.getKey().getPathId();
    		Path path = model.getPath(pathId);
    		if (path == null) {
        		logger.info("buildDataSet; can't get path for pathId: {}", pathId);
        		continue;
    		}
    		
    		for (Element element: entry.getValue().getElements()) {
        		int parentId = path.getParentId();
    			Data data = new Data(path, element);
				dataMap.put(element, data);
    			dataList.add(data);
    			Element elt = element;
    			while (parentId > 0) { //1
    				int[] position = elt.getPosition();
    				elt = new Element(Arrays.copyOf(position, position.length - 1), null);
    				if (dataMap.containsKey(elt)) {
    					break;
    				} else {
    					Path parent = model.getPath(parentId);
    					if (parent != null) {
    						data = new Data(parent, elt);
    						dataMap.put(elt, data);
    						dataList.add(data);
    						parentId = parent.getParentId();
    					}
    	    		}
    			}
    		}
    		
    	}
    	Collections.sort(dataList);
    	logger.trace("buildDataList; returning: {}", dataList);
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
