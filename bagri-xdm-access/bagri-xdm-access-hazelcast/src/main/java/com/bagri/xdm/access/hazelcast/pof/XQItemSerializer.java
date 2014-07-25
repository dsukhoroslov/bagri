package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;

import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;





import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xqj.BagriXQUtils;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
//import com.bagri.xqj.BagriXQDataFactory;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XQItemSerializer implements StreamSerializer<XQItem> {
	
    private static final Logger logger = LoggerFactory.getLogger(XQItemSerializer.class);
	

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XQItem;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	private XQDataFactory getXQDataFactory() {
		// @TODO: take it from context somehow!
		//HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName("TPoX");
		//ApplicationContext ctx = hz.getUserContext().get("appContext");
		//logger.trace("getXQDataFactory; hz: {}; context: {}", hz, hz.getUserContext());
		//return (XQDataFactory) hz.getUserContext().get("xqConnection");
		return BagriXQUtils.getXQDataFactory();
	}

	@Override
	public XQItem read(ObjectDataInput in) throws IOException {
		try {
			XQItemType type = in.readObject();
			String value = in.readUTF();
			XQDataFactory xqFactory = getXQDataFactory();
			return xqFactory.createItemFromString(value, type);
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}
	
	@Override
	public void write(ObjectDataOutput out, XQItem item) throws IOException {
		try {
			out.writeObject(item.getItemType());
			out.writeUTF(item.getItemAsString(null));
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}


}
