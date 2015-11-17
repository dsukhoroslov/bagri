package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.xdm.api.XDMTransactionIsolation;
import com.bagri.xdm.api.XDMTransactionState;
import com.bagri.xdm.domain.XDMTransaction;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMTransactionSerializer implements StreamSerializer<XDMTransaction> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMTransaction;
	}

	@Override
	public XDMTransaction read(ObjectDataInput in) throws IOException {
		
		return new XDMTransaction(in.readLong(),
				in.readLong(),
				in.readLong(),
				in.readUTF(),
				XDMTransactionIsolation.values()[in.readInt()],
				XDMTransactionState.values()[in.readInt()]);
	}

	@Override
	public void write(ObjectDataOutput out, XDMTransaction xTrans) throws IOException {
		
		out.writeLong(xTrans.getTxId());
		out.writeLong(xTrans.getStartedAt());
		out.writeLong(xTrans.getFinishedAt());
		out.writeUTF(xTrans.getStartedBy());
		out.writeInt(xTrans.getTxIsolation().ordinal());
		out.writeInt(xTrans.getTxState().ordinal());
	}

}
