package com.bagri.core.system;

import java.util.Properties;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * The adapter converting XDM Entries to Properties
 * 
 * @author Denis Sukhoroslov
 *
 */
public class EntriesAdapter extends XmlAdapter<Entries, Properties> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Entries marshal(Properties props) throws Exception {
	    Entries xdmProps = new Entries();
	    for (String name : props.stringPropertyNames()) {
	    	xdmProps.addEntry(new Entry(name, props.getProperty(name)));
	    }
	    return xdmProps;	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Properties unmarshal(Entries xdmProps) throws Exception {
		Properties props = new Properties();
	    for (Entry xdmProp : xdmProps.entries()) {
	    	props.setProperty(xdmProp.getName(), xdmProp.getValue());
	    }
	    return props;
	}

}
