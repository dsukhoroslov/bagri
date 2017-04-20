package com.bagri.core.server.api.df.json;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.BagriException;
import com.bagri.core.server.api.ContentModelProcessor;
import com.bagri.core.server.api.ModelManagement;

public class JsonModelProcessor implements ContentModelProcessor {

    private static final transient Logger logger = LoggerFactory.getLogger(JsonModelProcessor.class);
	
	protected ModelManagement modelMgr;
	
	/**
	 * 
	 * @param model the model management component
	 */
	public JsonModelProcessor(ModelManagement modelMgr) {
		//super(model);
		this.modelMgr = modelMgr;
	}
	
	
	@Override
	public void init(Properties properties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerModel(String model) throws BagriException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerModelUri(String modelUri) throws BagriException {
		// TODO Auto-generated method stub
		
	}

}
