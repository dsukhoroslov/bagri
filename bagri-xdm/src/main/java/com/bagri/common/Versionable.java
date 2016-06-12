package com.bagri.common;

import java.util.Date;

/**
 * An entity which can have many versions
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface Versionable {
	
	/**
	 * 
	 * @return the version number
	 */
	int getVersion();
	
	/**
	 * 
	 * @return the dat/time when version was created 
	 */
	Date getCreatedAt();
	
	/**
	 * 
	 * @return the user who has created the version
	 */
	String getCreatedBy();
	
	/**
	 * to update entity: create a new entity version
	 * 
	 * @param by the user who updates the version
	 */
	void updateVersion(String by);

}
