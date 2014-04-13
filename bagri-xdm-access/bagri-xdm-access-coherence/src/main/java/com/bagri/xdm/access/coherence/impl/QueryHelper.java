package com.bagri.xdm.access.coherence.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Set;

import com.tangosol.net.BackingMapContext;
import com.tangosol.net.cache.BackingMapBinaryEntry;
import com.tangosol.util.Binary;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.Filter;
import com.tangosol.util.ImmutableArrayList;
import com.tangosol.util.InvocableMapHelper;
import com.tangosol.util.ObservableMap;
import com.tangosol.util.SubSet;
import com.tangosol.util.comparator.EntryComparator;
import com.tangosol.util.comparator.SafeComparator;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.filter.IndexAwareFilter;
import com.tangosol.util.filter.LimitFilter;

public class QueryHelper {

	public static final QueryHelper INSTANCE = new QueryHelper();

	@SuppressWarnings({ "unchecked" })
	public <T> Set<T> query(BackingMapContext backingMapContext, Filter filter,
			boolean shouldReturnEntries, boolean shouldSort,
			Comparator comparator) {

		Map backingMap = backingMapContext.getBackingMap();
		Map indexMap = backingMapContext.getIndexMap();

		boolean matchAll = AlwaysFilter.INSTANCE.equals(filter);

		Filter remainingFilter = null;
		Object[] results;
		if (matchAll || indexMap == null || !(filter instanceof IndexAwareFilter)) {
			results = backingMap.keySet().toArray();
		} else {
			Set filteredKeys = new SubSet(backingMap.keySet());
			try {
				remainingFilter = ((IndexAwareFilter) filter).applyIndex(indexMap, filteredKeys);
			} catch (ConcurrentModificationException e) {
				filteredKeys = new SubSet(new ImmutableArrayList(backingMap.keySet().toArray()));
				remainingFilter = ((IndexAwareFilter) filter).applyIndex(indexMap, filteredKeys);
			}
			results = filteredKeys.toArray();
			matchAll = (remainingFilter == null);
		}

		int numberOfResults = 0;
		if (matchAll && !shouldReturnEntries) {
			numberOfResults = results.length;
		} else {
			for (Object key : results) {
				Object value = backingMap.get(key);
				if (value == null && !backingMap.containsKey(key)) {
					continue;
				}
				Map.Entry entry = new QueryBinaryEntry((Binary) key, (Binary) value, null, backingMapContext);
				if (matchAll || InvocableMapHelper.evaluateEntry(remainingFilter, entry)) {
					results[numberOfResults++] = shouldReturnEntries ? entry : key;
				}
			}
		}

		boolean isLimitFilter = filter instanceof LimitFilter;

		if (isLimitFilter || (shouldReturnEntries && shouldSort)) {
			if (numberOfResults < results.length) {
				Object[] copy = new Object[numberOfResults];
				System.arraycopy(results, 0, copy, 0, numberOfResults);
				results = copy;
			}

			if (shouldReturnEntries && shouldSort) {
				if (comparator == null) {
					comparator = SafeComparator.INSTANCE;
				}
				Arrays.sort(results, new EntryComparator(comparator));
			} else if (shouldSort) {
				Arrays.sort(results, comparator);
			}

			if (isLimitFilter) {
				LimitFilter limitFilter = (LimitFilter) filter;
				limitFilter.setComparator(null);
				results = limitFilter.extractPage(results);
				numberOfResults = results.length;
				limitFilter.setComparator(comparator);
			}
		}

		return new ImmutableArrayList(results, 0, numberOfResults).getSet();
	}

	public <T> Set<T> keySet(BinaryEntry entry, String cacheName, Filter filter) {
		return keySet(entry, cacheName, filter, false, null);
	}

	public <T> Set<T> keySet(BinaryEntry entry, String cacheName,
			Filter filter, boolean shouldSort, Comparator comparator) {
		
		BackingMapContext backingMapContext = entry.getContext().getBackingMapContext(cacheName);
		return query(backingMapContext, filter, false, shouldSort, comparator);
	}

	public Set entrySet(BinaryEntry entry, String cacheName, Filter filter) {
		return entrySet(entry, cacheName, filter, false, null);
	}

	public Set entrySet(BinaryEntry entry, String cacheName, Filter filter,
			boolean shouldSort, Comparator comparator) {
		
		BackingMapContext backingMapContext = entry.getContext().getBackingMapContext(cacheName);
		return query(backingMapContext, filter, true, shouldSort, comparator);
	}

	public <T> Set<T> entrySet(BackingMapContext backingMapContext,	Filter filter) {
		return query(backingMapContext, filter, true, false, null);
	}

	private class QueryBinaryEntry extends BackingMapBinaryEntry {
		private BackingMapContext backingMapContext;

		private QueryBinaryEntry(Binary key, Binary value,
				Binary originalValue, BackingMapContext backingMapContext) {
			super(key, value, originalValue, backingMapContext
					.getManagerContext());
			this.backingMapContext = backingMapContext;
		}

		@Override
		public ObservableMap getBackingMap() {
			return backingMapContext.getBackingMap();
		}

		@Override
		public BackingMapContext getBackingMapContext() {
			return backingMapContext;
		}

		@Override
		public boolean isReadOnly() {
			return false; //true;
		}

		@Override
		public boolean isPresent() {
			return true;
		}

	}
}
