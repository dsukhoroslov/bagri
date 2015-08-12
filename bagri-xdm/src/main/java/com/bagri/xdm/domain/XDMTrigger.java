package com.bagri.xdm.domain;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMRepository;

public interface XDMTrigger {

	void beforeInsert(XDMDocument doc, XDMRepository repo) throws XDMException;
	void afterInsert(XDMDocument doc, XDMRepository repo) throws XDMException;
	void beforeUpdate(XDMDocument doc, XDMRepository repo) throws XDMException;
	void afterUpdate(XDMDocument doc, XDMRepository repo) throws XDMException;
	void beforeDelete(XDMDocument doc, XDMRepository repo) throws XDMException;
	void afterDelete(XDMDocument doc, XDMRepository repo) throws XDMException;
	
}
