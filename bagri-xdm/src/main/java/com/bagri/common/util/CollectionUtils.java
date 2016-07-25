package com.bagri.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A set of static utility methods regarding collections
 * 
 * @author Denis Sukhoroslov
 *
 */
public class CollectionUtils {

	/**
	 * Converts Iterator to List of the same values
	 * 
	 * @param source the source Iterator to copy data from
	 * @param <T> the type of iterable instances
	 * @return the List containing values copied from the source Iterator 
	 */
	public static <T> List<T> copyIterator(Iterator<T> source) {
	    List<T> copy = new ArrayList<T>();
	    while (source.hasNext()) {
	        copy.add(source.next());
	    }
	    return copy;
	}
	
	/**
	 * Copies first {@value limit} values from Iterator to the List
	 * 
	 * @param source the source Iterator to copy data from
	 * @param limit the max number of values to copy from Iterator to the resulting List
	 * @param <T> the type of iterable instances
	 * @return the List containing first {@value limit} values copied from the source Iterator 
	 */
	public static <T> List<T> copyIterator(Iterator<T> source, int limit) {
	    List<T> copy = new ArrayList<T>(limit);
	    int cnt = 0;
	    while (source.hasNext() && cnt < limit) {
	        copy.add(source.next());
	        cnt++;
	    }
	    return copy;
	}

	/**
	 * Copies Iterator to List of the same values
	 * 
	 * @param source the source Iterator to copy data from
	 * @param target the List containing values copied from the source Iterator 
	 * @param <T> the type of iterable instances
	 */
	public static <T> void copyIterator(Iterator<T> source, List<T> target) {
	    while (source.hasNext()) {
	        target.add(source.next());
	    }
	}
	
	/**
	 * Converts Collection of Integers to int array
	 * 
	 * @param source the source Collection to copy data from
	 * @return the int array containing values copied from source
	 */
	public static int[] toIntArray(Collection<Integer> source) {
		int[] result = new int[source.size()];
		int idx = 0;
		for (Integer i: source) {
			result[idx++] = i;
		}
		return result;
	}
	
	/**
	 * Converts array of ints to List of Integers
	 * 
	 * @param source the source array to copy data from
	 * @return the List containing values copied from the source array
	 */
	public static List<Integer> toIntList(int[] source) {
		List<Integer> result = new ArrayList<>(source.length);
		for (int i: source) {
			result.add(i);
		}
		return result;
	}
	
	/**
	 * Copies data from array of longs to Collection of Longs
	 * 
	 * @param source the array to copy data from
	 * @param target the Collection to copy data to
	 */
	public static void fromLongArray(long[] source, Collection<Long> target) {
		for (long id: source) {
			target.add(id);
		}
	}

	/**
	 * Converts Collection of Longs to long array
	 * 
	 * @param source the source Collection to copy data from
	 * @return the long array containing values copied from source
	 */
	public static long[] toLongArray(Collection<Long> source) {
		long[] result = new long[source.size()];
		int idx = 0;
		for (Long l: source) {
			result[idx++] = l;
		}
		return result;
	}

	/**
	 * Converts array of longs to List of Longs
	 * 
	 * @param source the source array to copy data from
	 * @return the List containing values copied from the source array
	 */
	public static List<Long> toLongList(long[] source) {
		List<Long> result = new ArrayList<>(source.length);
		for (long l: source) {
			result.add(l);
		}
		return result;
	}

}
