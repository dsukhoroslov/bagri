package com.bagri.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CollectionUtils {

	public static <T> List<T> copyIterator(Iterator<T> iter) {
	    List<T> copy = new ArrayList<T>();
	    while (iter.hasNext())
	        copy.add(iter.next());
	    return copy;
	}
	
	public static long[] toLongArray(Collection<Long> source) {
		long[] result = new long[source.size()];
		int idx = 0;
		for (Long l: source) {
			result[idx] = l;
			idx++;
		}
		return result;
	}
}
