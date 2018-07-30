package com.bagri.xquery.saxon;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.functions.DeepEqual;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.AbstractItem;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AtomicIterator;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class MapItemImpl extends AbstractItem implements MapItem {
	
    private static final Logger logger = LoggerFactory.getLogger(MapItemImpl.class);
	
    private static final MapType type = new MapType(BuiltInAtomicType.STRING, SequenceType.ANY_SEQUENCE); 
    
	private Map<String, Object> source;
	private Configuration config;
	
	public MapItemImpl(Map<String, Object> source, Configuration config) {
		this.source = source;
		this.config = config;
		logger.trace("<init>. created MapItem from map: {}", source);
	}

	@Override
	public AnnotationList getAnnotations() {
        return AnnotationList.EMPTY;
	}

	//@Override
	//public UnfailingIterator iterate() {
		// TODO Auto-generated method stub
	//	return null;
	//}

	@Override
	public boolean isMap() {
		return true;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public FunctionItemType getFunctionItemType() {
        return type; 
	}

	@Override
	public StructuredQName getFunctionName() {
		logger.trace("getFunctionName.enter; returning null");
		return null;
	}

	@Override
	public int getArity() {
		return 1;
	}

	@Override
	public OperandRole[] getOperandRoles() {
        return new OperandRole[] {OperandRole.SINGLE_ATOMIC};
	}

	@Override
	public Sequence call(XPathContext context, Sequence[] args) throws XPathException {
		logger.trace("call.enter; got args: {}", args == null ? null : Arrays.toString(args));
        AtomicValue key = (AtomicValue) args[0].head();
        Sequence value = get(key);
        if (value == null) {
            return EmptySequence.getInstance();
        } else {
            return value;
        }
	}

	@Override
	public boolean deepEquals(Function other, XPathContext context, AtomicComparer comparer, int flags) throws XPathException {
		logger.trace("deepEquals.enter; other: {}; comparer: {}; flags: {}", other, comparer, flags);
        if (other instanceof MapItem && ((MapItem) other).size() == size()) {
            AtomicIterator keys = keys();
            AtomicValue key;
            while ((key = keys.next()) != null) {
                Sequence thisValue = get(key);
                Sequence otherValue = ((MapItem) other).get(key);
                if (otherValue == null) {
                    return false;
                }
                if (!DeepEqual.deepEqual(otherValue.iterate(), thisValue.iterate(), comparer, context, flags)) {
                    return false;
                }
            }
            return true;
        }
        return false;
	}

	@Override
	public String getDescription() {
		return "map";
	}

	@Override
	public void export(ExpressionPresenter out) throws XPathException {
		logger.trace("export.enter; out: {}", out);
        out.startElement("map");
        out.emitAttribute("size", size() + "");
        out.endElement();
	}

	@Override
	public boolean isTrustedResultType() {
		return true;
	}

	//@Override
	//public Item head() {
		// TODO Auto-generated method stub
	//	return null;
	//}

	@Override
	public String getStringValue() {
        throw new UnsupportedOperationException("A map has no string value");
	}

	@Override
	public CharSequence getStringValueCS() {
        throw new UnsupportedOperationException("A map has no string value");
	}

	@Override
	public AtomicSequence atomize() throws XPathException {
		throw new XPathException("atomization is not supported");
	}

	@Override
	public Item itemAt(int n) {
		logger.trace("getItemAt.enter; n: {}", n);
        return n == 0 ? this : null;
		//int idx = 0;
		//for (Map.Entry<String, Object> e: source.entrySet()) {
		//	if (idx == n) {
		//		try {
		//			return SaxonUtils.objectToItem(e.getValue(), config);
		//		} catch (XPathException ex) {
		//			logger.error("itemAt.error; n = {}, idx = {}", n, idx, ex);
		//			return null;
		//		}
		//	}
		//	idx++;
		//}
		//return null;
	}

	//@Override
	//public GroundedValue subsequence(int start, int length) {
		// TODO Auto-generated method stub
	//	return null;
	//}

	//@Override
	//public int getLength() {
	//	return source.size();
	//}

	@Override
	public boolean effectiveBooleanValue() throws XPathException {
		throw new XPathException("no effective boolean value");
	}

	@Override
	public XPathContext makeNewContext(XPathContext callingContext) {
		return callingContext;
	}

	@Override
	public GroundedValue reduce() {
		return this;
	}

	@Override
	public Sequence get(AtomicValue key) {
		logger.trace("get.enter; key: {}", key);
		Sequence result = null;
		String sKey = key.getStringValue();
		if (sKey.startsWith("@")) {
			sKey = sKey.substring(1);
		}
		Object value = source.get(sKey);
		if (value != null) {
			try {
				result = SaxonUtils.objectToItem(value, config);
			} catch (XPathException ex) {
				logger.error("get.error; key: {}", key, ex); 
			}
		} else if ("map".equals(sKey)) {
			result = this;
		}
		logger.trace("get.exit; returning {} for key: {}", result, key);
		return result;
	}

	@Override
	public int size() {
		logger.trace("size.enter; size: {}", source.size());
		return source.size();
	}

	@Override
	public boolean isEmpty() {
		return source.isEmpty();
	}

	@Override
	public AtomicIterator keys() {
		logger.trace("keys.enter");
		return new MapKeyIterator(source.keySet());
	}

	@Override
	public Iterator<KeyValuePair> iterator() {
		logger.trace("iterator.enter");
		return new MapKeyValueIterator(source.entrySet()); 
	}

	@Override
	public MapItem addEntry(AtomicValue key, Sequence value) {
		try {
			String sKey = key.getStringValue();
			Object sVal = SaxonUtils.itemToObject((Item) value);
			source.put(sKey, sVal);
		} catch (XPathException ex) {
			logger.error("addEntry.error; key: {}, value: {}", key, value, ex);
		}
		return this;
	}

	@Override
	public MapItem remove(AtomicValue key) {
		logger.trace("remove.enter; key: {}", key);
		source.remove(key.getStringValue());
		return this;
	}

	@Override
	public boolean conforms(AtomicType keyType, SequenceType valueType, TypeHierarchy th) {
		if (keyType.getUType() != UType.STRING) {
			return false;
		}
		if (valueType.getPrimaryType().getUType() != UType.ANY_ATOMIC) {
			return false;
		}
		return true;
	}

	@Override
	public UType getKeyUType() {
		return UType.STRING;
	}

	//@Override
	//public AtomicType getKeyType() {
	//	return BuiltInAtomicType.STRING;
	//}

	//@Override
	//public SequenceType getValueType() {
	//	return SequenceType.ANY_SEQUENCE;
	//}

	@Override
	public MapType getItemType(TypeHierarchy th) {
		return type;
	}

	Map<String, Object> getSource() {
		return source;
	}
	
	private class MapKeyIterator implements AtomicIterator {
		
		private Iterator<String> keys;
		
		MapKeyIterator(Set<String> keys) {
			this.keys = keys.iterator();
		}

		@Override
		public void close() {
		}

		@Override
		public int getProperties() {
			return 0;
		}

		@Override
		public AtomicValue next() {
			logger.trace("MapKeyIterator.next.enter");
			String key = null;
			try {
				key = keys.next();
			} catch (NoSuchElementException ex) {
				// noop
			}
			AtomicValue result = null;
			if (key != null) {
				result = new StringValue(key);
			}
			logger.trace("MapKeyIterator.next.exit; returning {}", result);
			return result;
		}

		//@Override
		//public AtomicIterator getAnother() {
		//	return null;
		//}
		
	};
	
	private class MapKeyValueIterator implements Iterator<KeyValuePair> {
		
		private Iterator<Map.Entry<String, Object>> pairs;
		
		MapKeyValueIterator(Set<Map.Entry<String, Object>> pairs) {
			this.pairs = pairs.iterator();
		}

		@Override
		public boolean hasNext() {
			return pairs.hasNext();
		}

		@Override
		public KeyValuePair next() {
			logger.trace("MapKeyValueIterator.next.enter");
			KeyValuePair result = null;
			Map.Entry<String, Object> pair = pairs.next();
			if (pair != null) {
				try {
					Item value = SaxonUtils.objectToItem(pair.getValue(), config);
					if (value == null) {
						result = new KeyValuePair(new StringValue(pair.getKey()), EmptySequence.getInstance()); 
					} else {
						result = new KeyValuePair(new StringValue(pair.getKey()), value);
					}
				} catch (XPathException ex) {
					logger.error("MapKeyValueIterator.next.error;", ex);
				}
			}
			logger.trace("MapKeyValueIterator.next.exit; returning {} for entry {}", result, pair);
			return result;
		}

		@Override
		public void remove() {
	        throw new UnsupportedOperationException("remove is not supported");
		}
		
	}

}
