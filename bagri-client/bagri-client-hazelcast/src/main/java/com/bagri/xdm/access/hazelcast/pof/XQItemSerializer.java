package com.bagri.xdm.access.hazelcast.pof;

import static com.bagri.xqj.BagriXQConstants.xs_ns;
import static com.bagri.xqj.BagriXQConstants.xs_prefix;
import static javax.xml.xquery.XQItemType.*;

import java.io.IOException;
import java.util.GregorianCalendar;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
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
			logger.trace("read; got type: {}", type); 
			
			int bType = type.getBaseType();
			if (BagriXQUtils.isAtomicType(type.getBaseType())) {
				switch (bType) {
					case XQBASETYPE_BASE64BINARY:
		    		case XQBASETYPE_HEXBINARY: 
						int len = in.readInt();
						byte[] ba = new byte[len];
						in.readFully(ba);
						return xqFactory.createItemFromObject(ba, type);
					case XQBASETYPE_BOOLEAN: 
						return xqFactory.createItemFromBoolean(in.readBoolean(), type);
					case XQBASETYPE_BYTE: 
						return xqFactory.createItemFromByte(in.readByte(), type);
					case XQBASETYPE_SHORT:
					case XQBASETYPE_UNSIGNED_BYTE:  
						return xqFactory.createItemFromShort(in.readShort(), type);
					case XQBASETYPE_INT: 
					case XQBASETYPE_UNSIGNED_SHORT:
						return xqFactory.createItemFromInt(in.readInt(), type);
					case XQBASETYPE_LONG: 
					case XQBASETYPE_UNSIGNED_INT: 
						return xqFactory.createItemFromLong(in.readLong(), type);
					case XQBASETYPE_INTEGER: 
					case XQBASETYPE_NEGATIVE_INTEGER: 
					case XQBASETYPE_NONNEGATIVE_INTEGER: 
					case XQBASETYPE_NONPOSITIVE_INTEGER: 
					case XQBASETYPE_POSITIVE_INTEGER: 
					case XQBASETYPE_UNSIGNED_LONG:
						// BigInteger
						return xqFactory.createItemFromObject(in.readObject(), type);
					case XQBASETYPE_DECIMAL:
						// BigDecimal
						return xqFactory.createItemFromObject(in.readObject(), type);
					case XQBASETYPE_DOUBLE: 
						return xqFactory.createItemFromDouble(in.readDouble(), type);
		    		case XQBASETYPE_FLOAT: 
						return xqFactory.createItemFromFloat(in.readFloat(), type);
		    		case XQBASETYPE_DATE: 
		    		case XQBASETYPE_DATETIME: 
		    		case XQBASETYPE_TIME: 
		    		case XQBASETYPE_GDAY: 
		    		case XQBASETYPE_GMONTH: 
		    		case XQBASETYPE_GMONTHDAY: 
		    		case XQBASETYPE_GYEAR: 
		    		case XQBASETYPE_GYEARMONTH: {
		    			// must be XMLGregorianCalendar
		    			GregorianCalendar gc = (GregorianCalendar) in.readObject();
		    			XMLGregorianCalendar xgc = BagriXQUtils.getXMLCalendar(gc, bType);
						return xqFactory.createItemFromObject(xgc, type);
		    		}					
		    		case XQBASETYPE_DURATION: 
		    		case XQBASETYPE_DAYTIMEDURATION: 
		    		case XQBASETYPE_YEARMONTHDURATION: {
		    			// must be string representation of Duration
		    			Duration xd = BagriXQUtils.getXMLDuration(in.readUTF(), bType);
						return xqFactory.createItemFromObject(xd, type);
		    		}
					case XQBASETYPE_QNAME: {
						QName qname = new QName(in.readUTF());
						return xqFactory.createItemFromObject(qname, type);
					}
				}
			} else {
				//
			}

			String value = in.readUTF();
			logger.trace("read; got value: {}", value); 
			return xqFactory.createItemFromString(value, type);
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}
	
	@Override
	public void write(ObjectDataOutput out, XQItem item) throws IOException {
		try {
			out.writeObject(item.getItemType());
			if (BagriXQUtils.isAtomicType(item.getItemType().getBaseType())) {
				switch (item.getItemType().getBaseType()) {
					case XQBASETYPE_BASE64BINARY: 
		    		case XQBASETYPE_HEXBINARY: {
						byte[] ba = (byte[]) item.getObject();
						out.writeInt(ba.length);
						out.write(ba);
						return;
					}
					case XQBASETYPE_BOOLEAN: {
						out.writeBoolean(item.getBoolean());
						return;
					}
					case XQBASETYPE_BYTE: { 
						out.writeByte(item.getByte());
						return;
					}
					case XQBASETYPE_INT: 
					case XQBASETYPE_UNSIGNED_SHORT: {
						out.writeInt(item.getInt());
						return;
					}
					case XQBASETYPE_LONG: 
					case XQBASETYPE_UNSIGNED_INT: { 
						out.writeLong(item.getLong());
						return;
					}
					case XQBASETYPE_SHORT: 
					case XQBASETYPE_UNSIGNED_BYTE: {  
						out.writeShort(item.getShort());
						return;
					}
					case XQBASETYPE_INTEGER: 
					case XQBASETYPE_NEGATIVE_INTEGER: 
					case XQBASETYPE_NONNEGATIVE_INTEGER: 
					case XQBASETYPE_NONPOSITIVE_INTEGER: 
					case XQBASETYPE_POSITIVE_INTEGER: 
					case XQBASETYPE_UNSIGNED_LONG: {
						// this must be BigInteger
						out.writeObject(item.getObject());
						return;
					}
					case XQBASETYPE_DOUBLE: {
						out.writeDouble(item.getDouble());
						return;
					}
		    		case XQBASETYPE_FLOAT: {
		    			out.writeFloat(item.getFloat());
		    			return;
		    		}
					case XQBASETYPE_DECIMAL: {
						// this must be BigDecimal
						out.writeObject(item.getObject());
						return;
					}
		    		case XQBASETYPE_DATE: 
		    		case XQBASETYPE_DATETIME: 
		    		case XQBASETYPE_TIME: 
		    		case XQBASETYPE_GDAY: 
		    		case XQBASETYPE_GMONTH: 
		    		case XQBASETYPE_GMONTHDAY: 
		    		case XQBASETYPE_GYEAR: 
		    		case XQBASETYPE_GYEARMONTH: {
		    			// must be XMLGregorianCalendar
		    			XMLGregorianCalendar xgc = (XMLGregorianCalendar) item.getObject();
		    			out.writeObject(xgc.toGregorianCalendar());
		    		}					
				}
			} else {
				//
			}
			out.writeUTF(item.getItemAsString(null));
			//out.writeObject(item.getObject());
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}


}
