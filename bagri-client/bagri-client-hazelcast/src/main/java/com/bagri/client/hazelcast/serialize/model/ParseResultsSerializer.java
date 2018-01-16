package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bagri.client.hazelcast.serialize.DomainSerializationFactory;
import com.bagri.core.model.Data;
import com.bagri.core.model.ParseResults;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class ParseResultsSerializer implements StreamSerializer<ParseResults> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DomainSerializationFactory.cli_XDMParseResults;
	}

	@Override
	public ParseResults read(ObjectDataInput in) throws IOException {
		int length = in.readInt();
		int size = in.readInt();
		List<Data> results = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			results.add((Data) in.readObject());
		}
		return new ParseResults(length, results);
	}

	@Override
	public void write(ObjectDataOutput out, ParseResults xRes) throws IOException {
		List<Data> results = xRes.getResults();
		out.writeInt(xRes.getContentLength());
		out.writeInt(results.size());
		for (Data data: results) {
			out.writeObject(data);
		}
	}


}
