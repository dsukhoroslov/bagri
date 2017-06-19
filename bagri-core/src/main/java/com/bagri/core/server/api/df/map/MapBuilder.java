package com.bagri.core.server.api.df.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.server.api.ContentBuilder;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentBuilderBase;

public class MapBuilder extends ContentBuilderBase<Map> implements ContentBuilder<Map<String, Object>> {

	MapBuilder(ModelManagement model) {
		super(model);
	}

	@Override
	public void init(Properties properties) {
		// think about possible props..
	}

	@Override
	public Map<String, Object> buildContent(Collection<Data> elements) throws BagriException {
		Map<String, Object> result = new HashMap<>(elements.size());
		// TODO: build nested maps, arrays..
		for (Data data: elements) {
			result.put(data.getName(), data.getValue());
		}
		return result;
	}

}
