package com.bagri.core.server.api.df.map;

import static javax.xml.xquery.XQItemType.*;
import static com.bagri.support.util.XQUtils.getBaseTypeForObject;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.xquery.XQException;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentParserBase;

public class MapParser extends ContentParserBase implements ContentParser<Map<String, Object>> {

	MapParser(ModelManagement model) {
		super(model);
	}

	@Override
	public void init(Properties properties) {
		// think about possible props..
	}

	@Override
	public List<Data> parse(Map<String, Object> source) throws BagriException {
		ParserContext ctx = initContext();
		//ctx.addDocument("/map");
		//ctx.addData("map"); 
		ctx.addDocument("/");
		parseMap(ctx, source);
		return ctx.getDataList();
	}
	
	private void parseMap(ParserContext ctx, Map content) throws BagriException {
		try {
			for (Object o: content.entrySet()) {
				Map.Entry e = (Map.Entry) o;
				String key = e.getKey().toString();
				Object value = e.getValue();
				int baseType = getBaseTypeForObject(value);
				if (baseType == XQBASETYPE_ANYTYPE) {
					// null
					ctx.addData(key);
					ctx.addValue();
				} else if (baseType == XQBASETYPE_ANYATOMICTYPE) {
					if (value instanceof Map) {
						ctx.addData(key);
						ctx.addElement(); 
						parseMap(ctx, (Map) value);
						ctx.endElement();
					} else if (value instanceof Collection) {
						if (((Collection) value).isEmpty()) {
							ctx.addData(key);
							ctx.addArray();
							ctx.endElement();
						} else {
							Object first = ((Collection) value).iterator().next();
							baseType = getBaseTypeForObject(first);
							if (baseType == XQBASETYPE_ANYATOMICTYPE) {
								ctx.addData(key);
								ctx.addArray();
								if (first instanceof Map) {
									Iterator itr = ((Collection) value).iterator();
									while (itr.hasNext()) {
										ctx.addElement();
										parseMap(ctx, (Map) itr.next());
										ctx.endElement();
									}
								} else {
									// ??
								}
								ctx.endElement();
							} else {
								ctx.addData("@" + key, NodeKind.attribute, value, baseType, Occurrence.zeroOrMany);
							}
						}
					}
					//Class<?> cls = value.getClass();
					//if (cls.isArray()) {
					//}
				} else {
					ctx.addData("@" + key, NodeKind.attribute, value, baseType, Occurrence.zeroOrOne);
				}
			}
		} catch (XQException | BagriException ex) {
			logger.error("parseMap.error", ex);
		}
	}

	@Override
	public List<Data> parse(File file) throws BagriException {
		// not used for Maps
		return null;
	}

	@Override
	public List<Data> parse(InputStream stream) throws BagriException {
		// not used for Maps
		return null;
	}

	@Override
	public List<Data> parse(Reader reader) throws BagriException {
		// not used for Maps
		return null;
	}

}
