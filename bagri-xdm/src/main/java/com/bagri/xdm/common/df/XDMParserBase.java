package com.bagri.xdm.common.df;

import static com.bagri.xquery.api.XQUtils.getAtomicValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.domain.XDMOccurrence;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath; 

/**
 * A common implementation part for any future parser. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class XDMParserBase {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected List<XDMData> dataList;
	protected Stack<XDMData> dataStack;
	protected XDMModelManagement model;
	protected int docType = -1;
	protected int elementId;
	
	/**
	 * 
	 * @param model the model management component. Used to search/add model paths.
	 */
	protected XDMParserBase(XDMModelManagement model) {
		this.model = model;
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
	protected XDMData addData(XDMData parent, XDMNodeKind kind, String name, String value, int dataType, XDMOccurrence occurence) throws XDMException {
		logger.trace("addData.enter; name: {}; kind: {}; value: {}; parent: {}", name, kind, value, parent);
		XDMElement xElt = new XDMElement();
		xElt.setElementId(elementId++);
		xElt.setParentId(parent.getElementId());
		String path = parent.getPath() + name;
		XDMPath xPath = model.translatePath(docType, path, kind, dataType, occurence);
		xElt.setValue(getAtomicValue(xPath.getDataType(), value));
		XDMData xData = new XDMData(xPath, xElt);
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
		dataList = new ArrayList<XDMData>();
		dataStack = new Stack<XDMData>();
		docType = -1;
		elementId = 0;
	}
	
	
}
