package com.bagri.xdm.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

    public void addElement(XDMElement element) {
          if (elementRef == null) {
                 elementRef = element;
                 return;
          }
          if (elementRef instanceof XDMElement) {
                 XDMElement oldElement = (XDMElement) elementRef;
                 elementRef = new HashMap<Long, XDMElement>();
                 ((Map<Long, XDMElement>) elementRef).put(oldElement.getElementId(), oldElement);
          }
          ((Map<Long, XDMElement>) elementRef).put(element.getElementId(), element);
          //if (element.getValue() != null) {
          //     values.put(element.getValue(), element.getElementId());
          //} // put null as some special object (static)
    }
   
    public Map<Long, XDMElement> getElements() {
          if (elementRef == null) {
        	  return null;
          }
          if (elementRef instanceof XDMElement) {
        	  Map<Long, XDMElement> elements = new HashMap<Long, XDMElement>();
        	  XDMElement element = (XDMElement) elementRef;
        	  elements.put(element.getElementId(), element);
              return elements;
          }
          return (Map<Long, XDMElement>) elementRef;
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
          String val = value.toString();
         
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
          
          if (elementRef instanceof XDMElement) {
        	  return compareValue(pex.getCompType(), val, ((XDMElement) elementRef).getValue());
          } else {
        	  return compareValues(pex.getCompType(), val, (Map<Long, XDMElement>) elementRef);
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

    private boolean compareValues(Comparison comp, String value, Map<Long, XDMElement> elements) {

        TreeMap<Object, Long> values = new TreeMap<Object, Long>();
        for (XDMElement element: elements.values()) {
        	if (element.getValue() != null) {
        		values.put(element.getValue(), element.getElementId());
        	}
        }
    	
        switch (comp) {
	        case EQ: return values.containsKey(value);
	        case NE: return !values.containsKey(value);
	        case LE: return values.floorKey(value) != null;
	        case LT: return values.lowerKey(value) != null;
	        case GE: return values.ceilingKey(value) != null;
	        case GT: return values.higherKey(value) != null;
	        default: return false;
        }
    }
    
    @Override
    public String toString() {
          return "XDMElements [pathId=" + pathId + ", elementRef=" + elementRef + "]";
    }
   
   
}
