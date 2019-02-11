package com.bagri.xquery.saxon;

import static com.bagri.xquery.saxon.SaxonUtils.objectToItem;
import static com.bagri.xquery.saxon.SaxonUtils.itemToObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.functions.DeepEqual;
import net.sf.saxon.ma.arrays.AbstractArrayItem;
import net.sf.saxon.ma.arrays.ArrayFunctionSet;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.om.AtomicArray;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.z.IntSet;

public class ArrayItemImpl extends AbstractArrayItem { //implements ArrayItem {
	
    private static final Logger logger = LoggerFactory.getLogger(ArrayItemImpl.class);
	
	private Configuration config;
	private List<Object> source;
	
	public ArrayItemImpl(List<Object> source, Configuration config) {
		this.source = source;
		this.config = config;
	}

	//@Override
	//public boolean isMap() {
	//	return false;
	//}

	//@Override
	//public boolean isArray() {
	//	return true;
	//}

	//@Override
	//public FunctionItemType getFunctionItemType() {
    //    return ArrayItemType.ANY_ARRAY_TYPE;
	//}

	//@Override
	//public StructuredQName getFunctionName() {
	//	return null;
	//}

	@Override
	public int getArity() {
		return 1;
	}

	@Override
	public OperandRole[] getOperandRoles() {
        return new OperandRole[] {OperandRole.SINGLE_ATOMIC};
	}

	@Override
	public GroundedValue<?> call(XPathContext context, Sequence[] arguments) throws XPathException {
		//	logger.trace("call.enter; got args: {}", args == null ? null : Arrays.toString(args));
        IntegerValue value = (IntegerValue) arguments[0].head();
        return get(ArrayFunctionSet.checkSubscript(value) - 1);
	}

	@Override
	public boolean deepEquals(Function other, XPathContext context, AtomicComparer comparer, int flags)	throws XPathException {
        if (other instanceof ArrayItem) {
            ArrayItem that = (ArrayItem) other;
            if (this.arrayLength() != that.arrayLength()) {
                return false;
            }
            for (int i = 0; i < this.arrayLength(); i++) {
                if (!DeepEqual.deepEqual(this.get(i).iterate(), that.get(i).iterate(), comparer, context, flags)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
	}

	//@Override
	//public String getDescription() {
	//	return "array";
	//}

	//@Override
	//public void export(ExpressionPresenter out) throws XPathException {
    //    out.startElement("array");
    //    out.emitAttribute("size", arrayLength() + "");
    //    for (Object o: source) {
    //        Literal.exportValue(objectToItem(o, config), out);
    //    }
    //    out.endElement();
	//}

	//@Override
	//public boolean isTrustedResultType() {
	//	return false;
	//}

	//@Override
	//public String getStringValue() {
    //    throw new UnsupportedOperationException("An array does not have a string value");
	//}

	//@Override
	//public CharSequence getStringValueCS() {
    //    throw new UnsupportedOperationException("An array does not have a string value");
	//}

	//@Override
	//public AnnotationList getAnnotations() {
	//	return null;
	//}

	@Override
	public AtomicSequence atomize() throws XPathException {
        List<AtomicValue> list = new ArrayList<>(source.size());
        for (Object o: source) {
            Item<?> item = objectToItem(o, config);
            SequenceIterator<?> iter = item.iterate();
            while ((item = iter.next()) != null) {
                AtomicSequence atoms = item.atomize();
                for (AtomicValue atom: atoms) {
                    list.add(atom);
                }
            }
        }
        return new AtomicArray(list);
	}

	@Override
	public boolean effectiveBooleanValue() throws XPathException {
        throw new XPathException("Effective boolean value is not defined for arrays");
	}

	@Override
	public GroundedValue<?> get(int index) throws XPathException {
        if (index < 0 || index >= source.size()) {
            throw new XPathException("Array index (" + (index+1) + ") out of range (1 to " + source.size() + ")");
        }
        return objectToItem(source.get(index), config);
	}

	@Override
	public int arrayLength() {
		return source.size();
	}

	@Override
	public boolean isEmpty() {
		return source.isEmpty();
	}

	@Override
	public Iterator<Function> iterator() {
		return new ArrayIterator(source);
	}

	@Override
	public ArrayItem concat(ArrayItem other) {
        //List<Object> list = new ArrayList<>(source.size() + other.arrayLength());
        //list.addAll(source);
        //if (other instanceof ArrayItemImpl) {
        //	for (Object o: ((ArrayItemImpl) other).source) {
        //		list.add(o);
        //	}
        //} else {
        //	try {
		//        for (int i=0; i < other.arrayLength(); i++) {
		//            list.add(itemToObject(other.itemAt(i)));
		//        }
        //	} catch (XPathException ex) {
		//		logger.error("concat.error;", ex);
        //	}
        //}
        //return new ArrayItemImpl(list, config);
        if (other instanceof ArrayItemImpl) {
        	for (Object o: ((ArrayItemImpl) other).source) {
        		source.add(o);
        	}
        } else {
    		for (int i=0; i < other.arrayLength(); i++) {
    			try {
    				source.add(itemToObject(other.itemAt(i)));
    		    } catch (XPathException ex) {
    				logger.error("concat.error;", ex);
    		    }
    		}
        }
        return this;
	}

	//@Override
	//public XPathContext makeNewContext(XPathContext callingContext, ContextOriginator originator) {
	//	return callingContext;
	//}

	@Override
	public ArrayItem remove(int index) {
		// remove inplace..
		source.remove(index);
		return this;
        //List<Object> list = new ArrayList<>(source.size() - 1);
        //list.addAll(source.subList(0, index));
        //list.addAll(source.subList(index + 1, source.size()));
        //return new ArrayItemImpl(list, config);
	}

	@Override
	public ArrayItem removeSeveral(IntSet positions) {
        //List<Object> list = new ArrayList<>(source.size() - positions.size());
        //for (int i=0; i < source.size(); i++) {
        //    if (!positions.contains(i)) {
        //        list.add(source.get(i));
        //    }
        //}
        //return new ArrayItemImpl(list, config);
        for (int i=source.size() - 1; i >= 0; i--) {
            if (positions.contains(i)) {
                source.remove(i);
            }
        }
		return this;
	}

	@Override
	public SequenceType getMemberType(TypeHierarchy th) {
		//...
		return SequenceType.ANY_SEQUENCE;
	}

	@Override
	public Iterable<GroundedValue<?>> members() {
    	List<GroundedValue<?>> list = new ArrayList<>(source.size());
    	try {
	    	for (Object o: source) {
	    		list.add(objectToItem(o, config));
	    	}
    	} catch (XPathException ex) {
			logger.error("getMembers.error;", ex);
    	}
       	return list;
	}
	
	List<Object> getSource() {
		return source;
	}
	
	@Override
	public ArrayItem insert(int position, GroundedValue<?> member) {
		try {
			source.add(position, itemToObject((Item<?>) member));
    	} catch (XPathException ex) {
			logger.error("insert.error;", ex);
    	}
		return this;
	}

	@Override
	public ArrayItem put(int index, GroundedValue<?> newValue) throws XPathException {
		source.set(index, itemToObject((Item<?>) newValue));
		return this;
	}

	@Override
	public ArrayItem subArray(int start, int end) {
		return new ArrayItemImpl(source.subList(start, end), config);
	}


	private class ArrayIterator implements Iterator<Function> {
		
		private Iterator<Object> itr;
		
		ArrayIterator(Collection<Object> source) {
			this.itr = source.iterator();
		}

		@Override
		public boolean hasNext() {
			return itr.hasNext();
		}

		@Override
		public Function next() {
			Object value = itr.next();
			if (value != null) {
				try {
					return ((Item<Function>) objectToItem(value, config)).head();
				} catch (XPathException ex) {
					logger.error("ArrayIterator.next.error;", ex);
				}
			}
			return null;
		}
	}

}
