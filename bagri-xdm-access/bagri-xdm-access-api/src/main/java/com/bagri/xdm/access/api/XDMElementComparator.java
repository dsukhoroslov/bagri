package com.bagri.xdm.access.api;

import java.util.Comparator;

import com.bagri.xdm.domain.XDMElement;

public class XDMElementComparator implements Comparator<XDMElement> {

	@Override
	public int compare(XDMElement o1, XDMElement o2) {
		
		return (int) (o1.getElementId() - o2.getElementId());
	}
	
}
