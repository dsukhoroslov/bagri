package com.bagri.xquery.saxon;

import static com.bagri.xquery.saxon.SaxonUtils.objectToItem;
import static com.bagri.xquery.saxon.SaxonUtils.itemToObject;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.ma.map.KeyValuePair;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.ma.map.MapType;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
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

public class MapItemImpl implements MapItem {
	
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
	public FunctionItemType getFunctionItemType() {
        return type; 
	}

	//@Override
	//public void export(ExpressionPresenter out) throws XPathException {
	//	logger.trace("export.enter; out: {}", out);
    //    out.startElement("map");
    //    out.emitAttribute("size", size() + "");
    //    out.endElement();
	//}

	//@Override
	//public AtomicSequence atomize() throws XPathException {
	//	throw new XPathException("atomization is not supported");
	//}

	//@Override
	//public MapItem itemAt(int n) {
	//	logger.trace("getItemAt.enter; n: {}", n);
    //    return n == 0 ? this : null;
	//}

	@Override
	public GroundedValue reduce() {
		return this;
	}

	@Override
	public GroundedValue<?> get(AtomicValue key) {
		//logger.trace("get.enter; key: {}", key);
		GroundedValue<?> result = null;
		String sKey = key.getStringValue();
		if (sKey.startsWith("@")) {
			sKey = sKey.substring(1);
		}
		Object value = source.get(sKey);
		if (value != null) {
			try {
				result = objectToItem(value, config);
			} catch (XPathException ex) {
				logger.error("get.error; key: {}", key, ex); 
			}
		} else if ("map".equals(sKey)) {
			result = this;
		}
		//logger.trace("get.exit; returning {} for key: {}", result, key);
		return result;
	}

	@Override
	public int size() {
		//logger.trace("size.enter; size: {}", source.size());
		return source.size();
	}

	@Override
	public boolean isEmpty() {
		return source.isEmpty();
	}

	@Override
	public AtomicIterator keys() {
		//logger.trace("keys.enter");
		return new MapKeyIterator(source.keySet());
	}

	//@Override
	//public Iterator<Function> iterator() {
		//logger.trace("iterator.enter");
	//	return new MapKeyValueIterator(source.entrySet()); 
	//}

	@Override
	public Iterable<KeyValuePair> keyValuePairs() {
		return new MapKeyValueIterator(source.entrySet()); 
	}

	@Override
	public MapItem addEntry(AtomicValue key, GroundedValue<?> value) {
		try {
			String sKey = key.getStringValue();
			Object sVal = itemToObject((Item<?>) value);
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
			//logger.trace("MapKeyIterator.next.enter");
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
			//logger.trace("MapKeyIterator.next.exit; returning {}", result);
			return result;
		}

		//@Override
		//public AtomicIterator getAnother() {
		//	return null;
		//}
		
	};
	
	private class MapKeyValueIterator implements Iterable<KeyValuePair>, Iterator<KeyValuePair> {
		
		private Iterator<Map.Entry<String, Object>> pairs;
		
		MapKeyValueIterator(Set<Map.Entry<String, Object>> pairs) {
			this.pairs = pairs.iterator();
		}

		@Override
		public Iterator<KeyValuePair> iterator() {
			return this;
		}
		
		@Override
		public boolean hasNext() {
			return pairs.hasNext();
		}

		@Override
		public KeyValuePair next() {
			//logger.trace("MapKeyValueIterator.next.enter");
			KeyValuePair result = null;
			Map.Entry<String, Object> pair = pairs.next();
			if (pair != null) {
				try {
					Item<?> value = objectToItem(pair.getValue(), config);
					if (value == null) {
						result = new KeyValuePair(new StringValue(pair.getKey()), EmptySequence.getInstance()); 
					} else {
						result = new KeyValuePair(new StringValue(pair.getKey()), value);
					}
				} catch (XPathException ex) {
					logger.error("MapKeyValueIterator.next.error;", ex);
				}
			}
			//logger.trace("MapKeyValueIterator.next.exit; returning {} for entry {}", result, pair);
			return result;
		}

		@Override
		public void remove() {
	        throw new UnsupportedOperationException("remove is not supported");
		}

	}

}
