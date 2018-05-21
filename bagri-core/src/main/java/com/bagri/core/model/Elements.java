package com.bagri.core.model;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import com.bagri.core.query.Comparison;
import com.bagri.core.query.PathExpression;


/**
 * Container for XDM elements.
 *  
 * @author Denis Sukhoroslov
 * @since 06.2014 
 */
public class Elements {

	private int pathId;
	private Object elementRef;

	/**
	 * default constructor
	 */
	public Elements() {
		//
	}

	/**
	 * 
	 * @param pathId the path id
	 * @param elements the Map of elements
	 */
	public Elements(int pathId, Collection<Element> elements) {
		this.pathId = pathId;
		setElements(elements);
	}

	/**
	 * 
	 * @param element the element to add into container
	 */
	@SuppressWarnings("unchecked")
	public void addElement(Element element) {
		if (elementRef == null) {
			elementRef = element;
			return;
		}
		if (elementRef instanceof Element) {
			Element oldElement = (Element) elementRef;
			elementRef = new ArrayList<>();
			((List<Element>) elementRef).add(oldElement);
		}
		((List<Element>) elementRef).add(element);
	}

	/**
	 * 
	 * @return container elements
	 */
	@SuppressWarnings("unchecked")
	public Collection<Element> getElements() {
		if (elementRef == null) {
			return Collections.emptyList();
		}
		if (elementRef instanceof Element) {
			// wouldn't be better to keep it in the Set then?
			ArrayList<Element> elements = new ArrayList<>();
			elements.add((Element) elementRef);
			return elements;
		}
		return (List<Element>) elementRef;
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
	public void setElements(Collection<Element> elements) {
		elementRef = null;
		if (elements != null && elements.size() > 0) {
			for (Element elt : elements) {
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

	@SuppressWarnings("unchecked")
	private boolean applyValue(PathExpression pex, Object value) {

		if (elementRef instanceof Element) {
			return compareValue(pex.getCompType(), value, ((Element) elementRef).getValue());
		} else {
			return compareValues(pex.getCompType(), value, (List<Element>) elementRef);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean compareValue(Comparison comp, Object value1, Object value2) {

		if (comp.isStringComparison()) {
			switch (comp) {
				case SW: return ((String) value2).startsWith((String) value1);
				case EW: return ((String) value2).endsWith((String) value1);
				case CNT: return ((String) value2).contains((String) value1);
				default: return false;
			}
		}

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

	private boolean compareValues(Comparison comp, Object value, List<Element> elements) {

		for (Element element : elements) {
			if (element.getValue() != null) {
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
		return "Elements [pathId=" + pathId + ", elementRef=" + elementRef + "]";
	}

}
