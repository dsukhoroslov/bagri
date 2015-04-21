package com.bagri.xdm.client.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.stream.events.XMLEvent;

import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;

public abstract class XDMDataParser {

	protected List<XDMData> dataList;
	protected Stack<XDMData> dataStack;
	protected XDMModelManagement dict;
	protected int docType = -1;
	protected long elementId;
	
	protected XDMDataParser(XDMModelManagement dict) {
		this.dict = dict;
	}
	
	protected XDMData addData(XDMData parent, XDMNodeKind kind, String name, String value) {

		XDMElement xElt = new XDMElement();
		xElt.setElementId(elementId++);
		xElt.setParentId(parent.getElementId());
		String path = parent.getPath() + name;
		xElt.setValue(value);
		XDMPath xPath = dict.translatePath(docType, path, kind);
		XDMData xData = new XDMData(xPath, xElt);
		dataList.add(xData);
		return xData;
	}
	
	protected void cleanup() {
		dataStack = null;
	}

	protected void init() {
		dataList = new ArrayList<XDMData>();
		dataStack = new Stack<XDMData>();
		docType = -1;
		elementId = 0;
	}
	
	
}
