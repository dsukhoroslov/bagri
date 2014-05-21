package com.bagri.xdm.system;

import java.util.Properties;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class XDMEntriesAdapter extends XmlAdapter<XDMEntries, Properties> {

	@Override
	public XDMEntries marshal(Properties props) throws Exception {
	    XDMEntries xdmProps = new XDMEntries();
	    for (String name : props.stringPropertyNames()) {
	    	xdmProps.addEntry(new XDMEntry(name, props.getProperty(name)));
	    }
	    return xdmProps;	
	}

	@Override
	public Properties unmarshal(XDMEntries xdmProps) throws Exception {
		Properties props = new Properties();
	    for (XDMEntry xdmProp : xdmProps.entries()) {
	    	props.setProperty(xdmProp.getName(), xdmProp.getValue());
	    }
	    return props;
	}

}
