package com.bagri.xdm.system;

import java.util.Properties;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The adapter converting XDM Entries to Properties
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XDMEntriesAdapter extends XmlAdapter<XDMEntries, Properties> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XDMEntries marshal(Properties props) throws Exception {
	    XDMEntries xdmProps = new XDMEntries();
	    for (String name : props.stringPropertyNames()) {
	    	xdmProps.addEntry(new XDMEntry(name, props.getProperty(name)));
	    }
	    return xdmProps;	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Properties unmarshal(XDMEntries xdmProps) throws Exception {
		Properties props = new Properties();
	    for (XDMEntry xdmProp : xdmProps.entries()) {
	    	props.setProperty(xdmProp.getName(), xdmProp.getValue());
	    }
	    return props;
	}

}
