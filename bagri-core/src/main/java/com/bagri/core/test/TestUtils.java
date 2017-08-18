package com.bagri.core.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.server.api.df.json.JsonpHandler;
import com.bagri.core.server.api.df.map.MapHandler;
import com.bagri.core.server.api.df.xml.XmlHandler;
import com.bagri.core.system.DataFormat;

public class TestUtils {

	public static Properties loadProperties(String fileName) throws IOException {
		Properties props = new Properties();
		//InputStream is = this.getClass().getResourceAsStream(fileName);
		InputStream is = new FileInputStream(fileName);
		props.load(is);
		return props;		
	}
	
	public static Collection<DataFormat> getBasicDataFormats() {
		ArrayList<DataFormat> cFormats = new ArrayList<>(2);
		ArrayList<String> cExt = new ArrayList<>(1);
		cExt.add("xml");
		DataFormat df = new DataFormat(1, new java.util.Date(), "", "XML", null, "application/xml", cExt, XmlHandler.class.getName(), true, null);
		cFormats.add(df);
		cExt = new ArrayList<>(1);
		cExt.add("json");
		df = new DataFormat(1, new java.util.Date(), "", "JSON", null, "application/json", cExt, JsonpHandler.class.getName(), true, null);
		cFormats.add(df);
		df = new DataFormat(1, new java.util.Date(), "", "MAP", null, null, null, MapHandler.class.getName(), true, null);
		cFormats.add(df);
		return cFormats;
	}
	
	public static int exploreCursor(ResultCursor cursor) throws Exception {
		int cnt = 0;
		while (cursor.next()) {
			String text = cursor.getItemAsString(null);
			System.out.println("" + cnt + ": " + text);
			cnt++;
		}
		return cnt;
	}
	
}
