package com.bagri.xdm.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.bagri.common.query.PathExpression;

public class XDMElements {

	//private long documentId;
	private int pathId;
	//private XDMNodeKind kind;
	//private String name;
	
	private Map<Long, XDMElement> elements = new HashMap<Long, XDMElement>();
	private TreeMap<Object, Long> values = new TreeMap<Object, Long>();
	
	public XDMElements() {
		//
	}
	
	public XDMElements(int pathId, Map<Long, XDMElement> elements) {
		this.pathId = pathId;
		setElements(elements);
	}

	public void addElement(XDMElement element) {
		elements.put(element.getElementId(), element);
		if (element.getValue() != null) {
			values.put(element.getValue(), element.getElementId());
		} // put null as some special object (static)
	}
	
	public Map<Long, XDMElement> getElements() {
		return elements;
	}
	
	public int getPathId() {
		return pathId;
	}
	
	public void setElements(Map<Long, XDMElement> elements) {
		this.elements.clear();
		if (elements != null) {
			this.elements.putAll(elements);
			// add values..
		}
	}

	public boolean apply(PathExpression pex, Object value) {
		// @TODO: add doctype and compare the real 
		// pex value, not its String representation
		
		//String field = "value";
		//Object value = pex.getValue().toString();
		String val = value.toString();
		
		//if (value instanceof Integer) {
		//	field = "asInt"; 
		//} else if (value instanceof Long) {
		//	field = "asLong";
		//} else if (value instanceof Boolean) {
		//	field = "asBoolean";
		//} else if (value instanceof Byte) {
		//	field = "asByte";
		//} else if (value instanceof Short) {
		//	field = "asShort";
		//} else if (value instanceof Float) {
		//	field = "asFloat";
		//} else if (value instanceof Double) {
		//	field = "asDouble";
		//} else {
		//	value = value.toString();
		//}
	
		switch (pex.getCompType()) {
			case EQ: return values.containsKey(val);
			case NE: return !values.containsKey(val);
			case LE: return values.floorKey(val) != null;
			case LT: return values.lowerKey(val) != null;
			case GE: return values.ceilingKey(val) != null;
			case GT: return values.higherKey(val) != null;
			default: return false;
		}
		
	}

	@Override
	public String toString() {
		return "XDMElements [pathId=" + pathId + ", elements=" + elements + "]";
	}
	
	
}
