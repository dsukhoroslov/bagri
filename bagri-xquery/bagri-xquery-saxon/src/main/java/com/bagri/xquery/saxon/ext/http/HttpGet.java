package com.bagri.xquery.saxon.ext.http;

import static com.bagri.xquery.saxon.SaxonUtils.sequence2Properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;

public class HttpGet extends HttpRequest {
	
	private static final transient Logger logger = LoggerFactory.getLogger(HttpGet.class);

	@Override
	protected String getFunctionName() {
		return "http-get";
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				logger.trace("call.enter; arguments: {}", Arrays.toString(arguments));
				String url = arguments[0].head().getStringValue();
				Properties headers = null;
				if (arguments.length > 1) {
					headers = sequence2Properties(arguments[1]); 
					logger.trace("call; headers: {}", headers);
				}
				
				try {
					URL obj = new URL(url);
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();
					con.setRequestMethod("GET");
					if (headers != null) {
						for (Object head: headers.keySet()) {
							String key = head.toString(); 
							con.setRequestProperty(key, headers.getProperty(key));
						}
					}
					int responseCode = con.getResponseCode();
					logger.debug("call; GET response {}; headers: {}", responseCode, con.getHeaderFields());
					try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
						String line;
						StringBuffer response = new StringBuffer();
						while ((line = in.readLine()) != null) {
							response.append(line);
						}
						// TODO: return response headers too? 
						return new StringValue(response.toString());
					}
					//return EmptySequence.getInstance();
				} catch (IOException ex) {
					logger.error("call.error on GET", ex);
					throw new XPathException(ex);
				}
				
			}
			
		};
	}

}
