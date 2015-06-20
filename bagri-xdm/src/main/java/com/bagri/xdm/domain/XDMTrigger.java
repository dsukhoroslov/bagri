package com.bagri.xdm.domain;

import com.bagri.xdm.api.XDMRepository;

public interface XDMTrigger {

	void beforeInsert(XDMDocument doc, XDMRepository repo);
	void afterInsert(XDMDocument doc, XDMRepository repo);
	void beforeUpdate(XDMDocument doc, XDMRepository repo);
	void afterUpdate(XDMDocument doc, XDMRepository repo);
	void beforeDelete(XDMDocument doc, XDMRepository repo);
	void afterDelete(XDMDocument doc, XDMRepository repo);
	
}
