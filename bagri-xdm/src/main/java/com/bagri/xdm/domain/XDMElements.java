package com.bagri.xdm.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.bagri.common.query.Comparison;
import com.bagri.common.query.PathExpression;

public class XDMElements {
	 
    //private long documentId;
    private int pathId;
    //private XDMNodeKind kind;
    //private String name;
   
    private Object elementRef;
    //private Map<Long, XDMElement> elements = new HashMap<Long, XDMElement>();
    //private TreeMap<Object, Long> values = new TreeMap<Object, Long>();
   
    public XDMElements() {
          //
    }
   
    public XDMElements(int pathId, Map<Long, XDMElement> elements) {
          this.pathId = pathId;
          setElements(elements);
    }

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
          //if (element.getValue() != null) {
          //     values.put(element.getValue(), element.getElementId());
          //} // put null as some special object (static)
    }
   
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
   
    public int getPathId() {
          return pathId;
    }
   
    public void setElements(Map<Long, XDMElement> elements) {
          elementRef = null;
          if (elements != null && elements.size() > 0) {
        	  for (XDMElement elt: elements.values()) {
        		  addElement(elt);
        	  }
          }
    }

    public boolean apply(PathExpression pex, Object value) {
          // @TODO: add doctype and compare the real
          // pex value, not its String representation
         
          //String field = "value";
          //Object value = pex.getValue().toString();
    	
          //if (value instanceof Integer) {
          //     field = "asInt";
          //} else if (value instanceof Long) {
          //     field = "asLong";
          //} else if (value instanceof Boolean) {
          //     field = "asBoolean";
          //} else if (value instanceof Byte) {
          //     field = "asByte";
          //} else if (value instanceof Short) {
          //     field = "asShort";
          //} else if (value instanceof Float) {
          //     field = "asFloat";
          //} else if (value instanceof Double) {
          //     field = "asDouble";
          //} else {
          //     value = value.toString();
          //}

    	if (value instanceof Collection) {
    		// TODO: implement this case..
    	}
        String val = value.toString();
    	
        if (elementRef instanceof XDMElement) {
        	return compareValue(pex.getCompType(), val, ((XDMElement) elementRef).getValue());
        } else {
        	return compareValues(pex.getCompType(), val, (Set<XDMElement>) elementRef);
        }
         
    }
    
    private boolean compareValue(Comparison comp, String value1, String value2) {

    	int result = value2.compareTo(value1);
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
	private boolean compareValues(Comparison comp, String value, Set<XDMElement> elements) {

    	// can we prevent this copy somehow?
        TreeSet values = new TreeSet();
        for (XDMElement element: elements) {
        	if (element.getValue() != null) {
        		values.add(element.getValue());
        	}
        }
    	
        switch (comp) {
	        case EQ: return values.contains(value);
	        case NE: return !values.contains(value);
	        case LE: return values.floor(value) != null;
	        case LT: return values.lower(value) != null;
	        case GE: return values.ceiling(value) != null;
	        case GT: return values.higher(value) != null;
	        default: return false;
        }
    }
    
    @Override
    public String toString() {
          return "XDMElements [pathId=" + pathId + ", elementRef=" + elementRef + "]";
    }
   
}
