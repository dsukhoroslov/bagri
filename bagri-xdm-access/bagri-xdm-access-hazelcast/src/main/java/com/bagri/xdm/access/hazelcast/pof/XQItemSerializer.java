package com.bagri.xdm.access.hazelcast.pof;

import static javax.xml.xquery.XQItemType.XQBASETYPE_BYTE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DECIMAL;
import static javax.xml.xquery.XQItemType.XQBASETYPE_INT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_LONG;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NEGATIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NONNEGATIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_NONPOSITIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_POSITIVE_INTEGER;
import static javax.xml.xquery.XQItemType.XQBASETYPE_SHORT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNSIGNED_BYTE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNSIGNED_INT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNSIGNED_LONG;
import static javax.xml.xquery.XQItemType.XQBASETYPE_UNSIGNED_SHORT;

import java.io.IOException;

import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xqj.BagriXQUtils;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XQItemSerializer implements StreamSerializer<XQItem> {
	
    private static final Logger logger = LoggerFactory.getLogger(XQItemSerializer.class);
	
	private XQDataFactory xqFactory;
	
	protected XQDataFactory getXQDataFactory() {
		// @TODO: take it from context somehow!
		//HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName("TPoX");
		//ApplicationContext ctx = hz.getUserContext().get("appContext");
		//logger.trace("getXQDataFactory; hz: {}; context: {}", hz, hz.getUserContext());
		//return (XQDataFactory) hz.getUserContext().get("xqConnection");
		//return BagriXQUtils.getXQDataFactory();
		return xqFactory;
	}

	public void setXQDataFactory(XQDataFactory xqDataFactory) {
		this.xqFactory = xqDataFactory;
	}

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XQItem;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public XQItem read(ObjectDataInput in) throws IOException {
		try {
			XQItemType type = in.readObject();
			String value = in.readUTF();
			logger.trace("read; got type: {}, value: {}", type, value); 
			//Object value = in.readObject();
			XQDataFactory xqFactory = getXQDataFactory();
			if (type != null && BagriXQUtils.isAtomicType(type.getBaseType())) {
				switch (type.getBaseType()) {
					case XQBASETYPE_BYTE: 
					case XQBASETYPE_SHORT:
					case XQBASETYPE_INT: 
					case XQBASETYPE_LONG: 
					case XQBASETYPE_DECIMAL:
					case XQBASETYPE_INTEGER: 
					case XQBASETYPE_NEGATIVE_INTEGER: 
					case XQBASETYPE_NONNEGATIVE_INTEGER: 
					case XQBASETYPE_NONPOSITIVE_INTEGER: 
					case XQBASETYPE_POSITIVE_INTEGER: 
					case XQBASETYPE_UNSIGNED_BYTE:  
					case XQBASETYPE_UNSIGNED_INT: 
					case XQBASETYPE_UNSIGNED_LONG:
					case XQBASETYPE_UNSIGNED_SHORT:
						return xqFactory.createItemFromLong(new Long(value), type);
					// TODO: process other atomic types here..
				}
			}
			return xqFactory.createItemFromString(value, type);
			//return xqFactory.createItemFromObject(value, type);
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}
	
	@Override
	public void write(ObjectDataOutput out, XQItem item) throws IOException {
		try {
			out.writeObject(item.getItemType());
			out.writeUTF(item.getItemAsString(null));
			//out.writeObject(item.getObject());
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}


}
