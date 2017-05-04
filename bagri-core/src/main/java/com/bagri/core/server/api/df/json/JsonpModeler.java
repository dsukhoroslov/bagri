package com.bagri.core.server.api.df.json;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.xml.xquery.XQItemType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ContentModeler;
import com.bagri.core.server.api.ModelManagement;

public class JsonpModeler implements ContentModeler {

    private static final transient Logger logger = LoggerFactory.getLogger(JsonpModeler.class);
    
    private JsonReaderFactory factory = Json.createReaderFactory(null);
	
	protected ModelManagement modelMgr;
	
	/**
	 * 
	 * @param modelMgr the model management component
	 */
	JsonpModeler(ModelManagement modelMgr) {
		//super(model);
		this.modelMgr = modelMgr;
	}
	
	
	@Override
	public void init(Properties properties) {
		// process/convert any specific properties here 
		Map<String, Object> params = new HashMap<>();
		for (Map.Entry prop: properties.entrySet()) {
			//String name = (String) prop.getKey();
			params.put((String) prop.getKey(), prop.getValue());
		}
		factory = Json.createReaderFactory(params);
	}

	@Override
	public void registerModel(String model) throws BagriException {
		JsonReader reader = factory.createReader(new StringReader(model));
		JsonObject schema = reader.readObject();
		processModel(schema);
		reader.close();
	}

	@Override
	public void registerModelUri(String modelUri) throws BagriException {
		try {
			JsonReader reader = factory.createReader(new FileInputStream(modelUri));
			JsonObject schema = reader.readObject();
			processModel(schema);
			reader.close();
		} catch (IOException ex) {
			throw new BagriException(ex, BagriException.ecInOut);
		}
	}
	
	private void processModel(JsonObject model) throws BagriException {
		String root = "/";
		Path xp = modelMgr.translatePath(root, "/", NodeKind.document, 0, XQItemType.XQBASETYPE_UNTYPED, Occurrence.onlyOne); 
		processObject(xp, "", model);
	}
	
	private void processObject(Path parent, String path, JsonObject object) throws BagriException {
		logger.debug("processObject; got object: {}", object);
		JsonArray mandatory = object.getJsonArray("required");
		JsonObject fields = object.getJsonObject("properties");
		//NodeKind kind = parent.getNodeKind() == NodeKind.array ? NodeKind.text : NodeKind.attribute;
		for (String field: fields.keySet()) {
			JsonObject value = fields.getJsonObject(field);
			Occurrence occ = mandatory.contains(field) ? Occurrence.onlyOne : Occurrence.zeroOrOne;
			processField(parent, path, field, value, occ);
			// it can be an array also!
			//String type = value.getString("type", null);
			//if (type != null) {
			//	String next = path + "/" + field;
			//	switch (type) {
			//		case "array":
			//			Path array = modelMgr.translatePath(parent.getRoot(), next, NodeKind.array, parent.getPathId(), XQItemType.XQBASETYPE_ANYTYPE, occ);
			//			JsonObject items = value.getJsonObject("items");
						// process array items..
			//			break;
			//		case "boolean":
			//			modelMgr.translatePath(parent.getRoot(), next, NodeKind.attribute, parent.getPathId(), XQItemType.XQBASETYPE_BOOLEAN, occ);
			//			break;
			//		case "integer":
			//			modelMgr.translatePath(parent.getRoot(), next, NodeKind.attribute, parent.getPathId(), XQItemType.XQBASETYPE_LONG, occ);
			//			break;
			//		case "number":
			//			modelMgr.translatePath(parent.getRoot(), next, NodeKind.attribute, parent.getPathId(), XQItemType.XQBASETYPE_DECIMAL, occ);
			//			break;
			//		case "object":
			//			Path element = modelMgr.translatePath(parent.getRoot(), next, NodeKind.element, parent.getPathId(), XQItemType.XQBASETYPE_ANYTYPE, occ);
			//			processObject(element, next, value);
			//			break;
			//		case "string":
			//			modelMgr.translatePath(parent.getRoot(), next, NodeKind.attribute, parent.getPathId(), XQItemType.XQBASETYPE_STRING, occ);
			//			break;
			//	}
			//}
		}
	}
	
	private void processField(Path parent, String path, String field, JsonObject object, Occurrence occ) throws BagriException {

		// it can be an array also!
		String type = object.getString("type", null);
		if (type != null) {
			if (!path.endsWith("/")) {
				path += "/";
			}
			String next = path + field;
			switch (type) {
				case "array":
					Path array = modelMgr.translatePath(parent.getRoot(), next, NodeKind.array, parent.getPathId(), XQItemType.XQBASETYPE_ANYTYPE, occ);
					JsonObject items = object.getJsonObject("items");
					processField(array, path, field + "/", items, occ);
					break;
				case "boolean":
					modelMgr.translatePath(parent.getRoot(), next, NodeKind.attribute, parent.getPathId(), XQItemType.XQBASETYPE_BOOLEAN, occ);
					break;
				case "integer":
					modelMgr.translatePath(parent.getRoot(), next, NodeKind.attribute, parent.getPathId(), XQItemType.XQBASETYPE_LONG, occ);
					break;
				case "number":
					modelMgr.translatePath(parent.getRoot(), next, NodeKind.attribute, parent.getPathId(), XQItemType.XQBASETYPE_DECIMAL, occ);
					break;
				case "object":
					Path element = modelMgr.translatePath(parent.getRoot(), next, NodeKind.element, parent.getPathId(), XQItemType.XQBASETYPE_ANYTYPE, occ);
					processObject(element, next, object);
					break;
				case "string":
					modelMgr.translatePath(parent.getRoot(), next, NodeKind.attribute, parent.getPathId(), XQItemType.XQBASETYPE_STRING, occ);
					break;
			}
		}
	}
}
