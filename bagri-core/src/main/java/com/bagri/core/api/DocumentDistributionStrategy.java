package com.bagri.core.api;

import java.io.Serializable;

/**
 * Hash generator
 * 
 * @author Denis SUkhoroslov
 *
 */
public interface DocumentDistributionStrategy extends Serializable {
	
	int getDistributionHash(String uri);

}
