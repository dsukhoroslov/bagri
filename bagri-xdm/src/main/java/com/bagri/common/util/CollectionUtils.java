package com.bagri.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CollectionUtils {

	public static <T> List<T> copyIterator(Iterator<T> iter) {
	    List<T> copy = new ArrayList<T>();
	    while (iter.hasNext()) {
	        copy.add(iter.next());
	    }
	    return copy;
	}
	
	public static int[] toIntArray(Collection<Integer> source) {
		int[] result = new int[source.size()];
		int idx = 0;
		for (Integer i: source) {
			result[idx++] = i;
		}
		return result;
	}
	
	public static List<Integer> toIntList(int[] source) {
		List<Integer> result = new ArrayList<>(source.length);
		for (int i: source) {
			result.add(i);
		}
		return result;
	}
	
	public static void fromLongArray(long[] source, Collection<Long> target) {
		for (long id: source) {
			target.add(id);
		}
	}

	public static long[] toLongArray(Collection<Long> source) {
		long[] result = new long[source.size()];
		int idx = 0;
		for (Long l: source) {
			result[idx++] = l;
		}
		return result;
	}

	public static List<Long> toLongList(long[] source) {
		List<Long> result = new ArrayList<>(source.length);
		for (long l: source) {
			result.add(l);
		}
		return result;
	}

}
