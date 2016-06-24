package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.bagri.xdm.common.query.Comparison;
import com.bagri.xdm.common.query.PathExpression;


/**
 * Container for XDM elements.
 *  
 * @author Denis Sukhoroslov
 * @since 06.2014 
 * @version 0.3
 */
public class XDMElements {

	private int pathId;
	private Object elementRef;

	/**
	 * default constructor
	 */
	public XDMElements() {
		//
	}

	/**
	 * 
	 * @param pathId the path id
	 * @param elements the Map of elements
	 */
	public XDMElements(int pathId, Map<Long, XDMElement> elements) {
		this.pathId = pathId;
		setElements(elements);
	}

	/**
	 * 
	 * @param element the element to add into container
	 */
	@SuppressWarnings("unchecked")
	public void addElement(XDMElement element) {
		if (elementRef == null) {
			elementRef = element;
			return;
		}
		if (elementRef instanceof XDMElement) {
			XDMElement oldElement = (XDMElement) elementRef;
			elementRef = new TreeSet<XDMElement>();
			((Set<XDMElement>) elementRef).add(oldElement);
		}
		((Set<XDMElement>) elementRef).add(element);
		// if (element.getValue() != null) {
		// values.put(element.getValue(), element.getElementId());
		// } // put null as some special object (static)
	}

	/**
	 * 
	 * @return container elements
	 */
	@SuppressWarnings("unchecked")
	public Collection<XDMElement> getElements() {
		if (elementRef == null) {
			return Collections.emptyList();
		}
		if (elementRef instanceof XDMElement) {
			Set<XDMElement> elements = new TreeSet<XDMElement>();
			elements.add((XDMElement) elementRef);
			return elements;
		}
		return (Set<XDMElement>) elementRef;
	}

	/**
	 * 
	 * @return element's path id
	 */
	public int getPathId() {
		return pathId;
	}

	/**
	 * 
	 * @param elements elements to set
	 */
	public void setElements(Map<Long, XDMElement> elements) {
		elementRef = null;
		if (elements != null && elements.size() > 0) {
			for (XDMElement elt : elements.values()) {
				addElement(elt);
			}
		}
	}

	/**
	 * performs comparison with expression 
	 * 
	 * @param pex the expression
	 * @param value the value to compare with
	 * @return true if comparison satisfies, false otherwise
	 */
	public boolean apply(PathExpression pex, Object value) {

		if (value instanceof Collection) {
			for (Object val: (Collection) value) {
				if (applyValue(pex, val)) {
					return true;
				}
			}
			return false;
		} else {
			return applyValue(pex, value);
		}
	}

	private boolean applyValue(PathExpression pex, Object value) {

		//String val = value.toString();
		if (elementRef instanceof XDMElement) {
			return compareValue(pex.getCompType(), value, ((XDMElement) elementRef).getValue());
		} else {
			return compareValues(pex.getCompType(), value, (Set<XDMElement>) elementRef);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean compareValue(Comparison comp, Object value1, Object value2) {

		int result = ((Comparable) value2).compareTo((Comparable) value1);
		switch (comp) {
			case EQ: return result == 0;
			case NE: return result != 0;
			case LE: return result <= 0;
			case LT: return result < 0;
			case GE: return result >= 0;
			case GT: return result > 0;
			default: return false;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean compareValues(Comparison comp, Object value, Set<XDMElement> elements) {

		// can we prevent this copy somehow?
		// yes!!
		//TreeSet values = new TreeSet();
		for (XDMElement element : elements) {
			if (element.getValue() != null) {
				//values.add(element.getValue());
				if (compareValue(comp, value, element.getValue())) {
					return true;
				}
			}
		}
		return false;

		//switch (comp) {
		//	case EQ: return values.contains(value);
		//	case NE: return !values.contains(value);
		//	case LE: return values.floor(value) != null;
		//	case LT: return values.lower(value) != null;
		//	case GE: return values.ceiling(value) != null;
		//	case GT: return values.higher(value) != null;
		//	default: return false;
		//}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMElements [pathId=" + pathId + ", elementRef=" + elementRef + "]";
	}

}
