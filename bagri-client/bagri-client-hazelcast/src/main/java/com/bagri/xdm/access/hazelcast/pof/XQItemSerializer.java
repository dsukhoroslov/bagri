package com.bagri.xdm.access.hazelcast.pof;

import static com.bagri.xqj.BagriXQConstants.xs_ns;
import static com.bagri.xqj.BagriXQConstants.xs_prefix;
import static javax.xml.xquery.XQItemType.XQBASETYPE_BASE64BINARY;
import static javax.xml.xquery.XQItemType.XQBASETYPE_BOOLEAN;
import static javax.xml.xquery.XQItemType.XQBASETYPE_BYTE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DECIMAL;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DOUBLE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DURATION;
import static javax.xml.xquery.XQItemType.XQBASETYPE_ENTITIES;
import static javax.xml.xquery.XQItemType.XQBASETYPE_ENTITY;
import static javax.xml.xquery.XQItemType.XQBASETYPE_FLOAT;
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

import javax.xml.namespace.QName;
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
					case XQBASETYPE_BASE64BINARY:
						// !! this is an array of..! must be written properly!
						return xqFactory.createItemFromObject(value, type);
					case XQBASETYPE_BOOLEAN: 
						return xqFactory.createItemFromBoolean(new Boolean(value), type);
					case XQBASETYPE_BYTE: 
						return xqFactory.createItemFromByte(new Byte(value), type);
					case XQBASETYPE_SHORT:
						return xqFactory.createItemFromShort(new Short(value), type);
					case XQBASETYPE_INT: 
					case XQBASETYPE_LONG: 
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

					case XQBASETYPE_DECIMAL:
						return xqFactory.createItemFromLong(new java.math.BigDecimal(value).longValue(), type);
					case XQBASETYPE_DOUBLE: 
						return xqFactory.createItemFromDouble(new Double(value), type);
		    		case XQBASETYPE_FLOAT: 
						return xqFactory.createItemFromFloat(new Float(value), type);
				}
			}
			return xqFactory.createItemFromString(value, type);
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}
	
	@Override
	public void write(ObjectDataOutput out, XQItem item) throws IOException {
		try {
			out.writeObject(item.getItemType());
			/*
			if (BagriXQUtils.isAtomicType(item.getItemType().getBaseType())) {
				switch (item.getItemType().getBaseType()) {
					case XQBASETYPE_BASE64BINARY:
						// !! this is an array of..! must be written properly!
						return xqFactory.createItemFromObject(value, type);
					case XQBASETYPE_BOOLEAN: 
						return xqFactory.createItemFromBoolean(new Boolean(value), type);
					case XQBASETYPE_BYTE: 
						return xqFactory.createItemFromByte(new Byte(value), type);
					case XQBASETYPE_SHORT:
						return xqFactory.createItemFromShort(new Short(value), type);
					case XQBASETYPE_INT: 
					case XQBASETYPE_LONG: 
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

					case XQBASETYPE_DECIMAL:
						return xqFactory.createItemFromLong(new java.math.BigDecimal(value).longValue(), type);
					case XQBASETYPE_DOUBLE: 
						return xqFactory.createItemFromDouble(new Double(value), type);
		    		case XQBASETYPE_FLOAT: 
						return xqFactory.createItemFromFloat(new Float(value), type);
				}
			}
			*/
			out.writeUTF(item.getItemAsString(null));
			//out.writeObject(item.getObject());
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}


}
