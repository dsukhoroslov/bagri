package com.bagri.client.hazelcast.serialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XQSequenceSerializer implements StreamSerializer<XQSequence> {

    private static final Logger logger = LoggerFactory.getLogger(XQSequenceSerializer.class);
	
	private XQDataFactory xqFactory;
	
	protected XQDataFactory getXQDataFactory() {
		return xqFactory;
	}

	public void setXQDataFactory(XQDataFactory xqDataFactory) {
		this.xqFactory = xqDataFactory;
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XQSequence;
	}

	@Override
	public void destroy() {
	}

	@Override
	public XQSequence read(ObjectDataInput in) throws IOException {
		try {
			//XQItemType type = in.readObject();
			List<XQItemAccessor> items = (List<XQItemAccessor>) in.readObject();
			logger.trace("read; got items: {}", items);
			return xqFactory.createSequence(items.iterator());
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public void write(ObjectDataOutput out, XQSequence sequence) throws IOException {
		try {
			List<XQItemAccessor> items;
			synchronized (sequence) {
				if (sequence.isScrollable()) {
					sequence.beforeFirst();
					items = new ArrayList<>(sequence.count());
				} else {
					items = new ArrayList<>();
				}
				while (sequence.next()) {
					Object value = sequence.getObject();
					if (value instanceof XQItemAccessor) {
						items.add((XQItemAccessor) value);
					} else {
						items.add(sequence.getItem());
					}
				}
			}
			logger.trace("write; writing items: {}", items);
			out.writeObject(items);
		} catch (XQException ex) {
			throw new IOException(ex);
		}
	}

}
