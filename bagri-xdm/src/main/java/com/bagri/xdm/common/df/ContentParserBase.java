package com.bagri.xdm.common.df;

import static com.bagri.xquery.api.XQUtils.getAtomicValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.ModelManagement;
import com.bagri.xdm.domain.Occurrence;
import com.bagri.xdm.domain.Data;
import com.bagri.xdm.domain.Element;
import com.bagri.xdm.domain.NodeKind;
import com.bagri.xdm.domain.Path; 

/**
 * A common implementation part for any future parser. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class ContentParserBase {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected List<Data> dataList;
	protected Stack<Data> dataStack;
	protected ModelManagement model;
	protected int docType = -1;
	protected int elementId;
	
	/**
	 * 
	 * @param model the model management component. Used to search/add model paths.
	 */
	protected ContentParserBase(ModelManagement model) {
		this.model = model;
	}
	
    /**
     * {@inheritDoc}
     */
 	public void init(Map<String, Object> context) {
 		//
 		logger.trace("init; got context: {}", context);
 	}
 	
	/**
	 * 
	 * @param parent parent data element
	 * @param kind a kind of creating element
	 * @param name creating element name
	 * @param value creating element value. Can be null for non leaf elements 
	 * @param dataType the value data type as per XQJ type constants
	 * @param occurence creating element cardinality 
	 * @return the created XDM data element
	 * @throws XDMException in case of any failure at path translation step
	 */
	protected Data addData(Data parent, NodeKind kind, String name, String value, int dataType, Occurrence occurence) throws XDMException {
		logger.trace("addData.enter; name: {}; kind: {}; value: {}; parent: {}", name, kind, value, parent);
		Element xElt = new Element();
		xElt.setElementId(elementId++);
		xElt.setParentId(parent.getElementId());
		String path = parent.getPath() + name;
		Path xPath = model.translatePath(docType, path, kind, dataType, occurence);
		xPath.setParentId(parent.getPathId());
		xElt.setValue(getAtomicValue(xPath.getDataType(), value));
		Data xData = new Data(xPath, xElt);
		dataList.add(xData);
		return xData;
	}
	
	/**
	 * cleans up paser components after parsing document
	 */
	protected void cleanup() {
		dataStack = null;
	}

	/**
	 * initializes parser components before parsing document
	 */
	protected void init() {
		dataList = new ArrayList<Data>();
		dataStack = new Stack<Data>();
		docType = -1;
		elementId = 0;
	}
	
	
}
