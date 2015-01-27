package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XQSequenceSerializer implements StreamSerializer<XQSequence> {

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
		return XDMPortableFactory.cli_XQSequence;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public XQSequence read(ObjectDataInput in) throws IOException {
		try {
			XQItemType type = in.readObject();
			List list = (List) in.readObject();
			logger.trace("read; got type: {}; list: {}", type, list);
			return xqFactory.createSequence(list.iterator());
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void write(ObjectDataOutput out, XQSequence sequence) throws IOException {
		try {
			XQItemType type = sequence.getItemType();
			out.writeObject(type);
			List list = new ArrayList();
			while (sequence.next()) {
				list.add(sequence.getItem());
			}
			logger.trace("write; got type: {}; list: {}", type, list);
			out.writeObject(list);
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}

}
