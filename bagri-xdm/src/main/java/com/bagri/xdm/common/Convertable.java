package com.bagri.xdm.common;

/**
 * An entity that can be converted to some other format
 * 
 * @author Denis Sukhoroslov
 *
 * @param <T> the conversion result type
 */
public interface Convertable<T> {

	/**
	 * converts the entity
	 * 
	 * @return the conversion result
	 */
	T convert();
	
}
