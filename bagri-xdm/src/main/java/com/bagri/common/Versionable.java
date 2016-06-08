package com.bagri.common;

import java.util.Date;

public interface Versionable {
	
	int getVersion();
	Date getCreatedAt();
	String getCreatedBy();
	void updateVersion(String by);

}
