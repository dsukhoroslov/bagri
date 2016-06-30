package com.bagri.common.idgen;

/**
 * The parameterized identity generator
 * 
 * @author Denis Sukhoroslov
 *
 * @param <T> the generated id type
 */
public interface IdGenerator<T> {

	/**
	 * Set generator to the value provided. If the current generator value is less then the provided one
	 * then it'll apply the new value.
	 * 
	 * @param newValue the new value for generator
	 * @return true if generator applies the new value, false otherwise
	 */
	boolean adjust(T newValue);
	
	/**
	 * Generates a new identifier
	 * 
	 * @return the new generated value
	 */
	T next();
	
	/**
	 * Generates an array of new identifiers
	 * 
	 * @param size the number of ids to generate
	 * @return an array of generated ids
	 */
	T[] nextRange(int size);
}
