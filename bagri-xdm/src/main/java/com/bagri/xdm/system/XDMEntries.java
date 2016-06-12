package com.bagri.xdm.system;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * An entity containing configuration entries
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/xdm/system", propOrder = {
		"props" 
})
public class XDMEntries {

	  @XmlElement(name = "entry")
	  private List<XDMEntry> props = new ArrayList<>();
	 
	  List<XDMEntry> entries() {
		  return props;
	  }
	 
	  void addEntry(XDMEntry entry) {
		  props.add(entry);
	  }

}