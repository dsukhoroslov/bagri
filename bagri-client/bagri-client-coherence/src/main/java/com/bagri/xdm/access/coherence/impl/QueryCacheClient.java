package com.bagri.xdm.access.coherence.impl;

import com.tangosol.coherence.dslquery.CoherenceQuery;
import com.tangosol.coherence.dslquery.CoherenceQueryLanguage;
import com.tangosol.coherence.dslquery.SQLOPParser;
import com.tangosol.coherence.dsltools.precedence.TokenTable;
import com.tangosol.coherence.dsltools.termtrees.NodeTerm;
import com.tangosol.coherence.dsltools.termtrees.Term;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Query Cache Client class
 */
public class QueryCacheClient { //implements QueryAPI {

	private static final Logger LOG = LoggerFactory.getLogger(QueryCacheClient.class);

    /**
     *
     * @param args Argument string array
     * @throws IOException in case of error
     */
    public static void main(String[] args) throws IOException {
    	
        BufferedReader in  = new BufferedReader(new InputStreamReader(System.in));
        PrintStream    out = System.out;
        
        String cacheName = null;
        for (String arg : args) {
        	out.println(">> got param: " + arg);
        	if (cacheName == null && arg.startsWith("cache=")) {
        		cacheName = arg.substring(6);
            	out.println(">> got cache name: " + cacheName);
        	}
        }

        //QueryAPI client;
        //if (cacheName == null) {
        //	client = new QueryCacheClient();
        //} else {
            //if (cacheName.equals("portfolio")) {// TODO: What is this?
            //	client = new NamedCacheClient<String, DataSet>(cacheName);
        	//} else {
        //		client = new NamedCacheClient(cacheName);
        	//}
        //}
        
	QueryCacheClient client = new QueryCacheClient();

        while (true) {
            out.println();
            out.print("Query: ");
            out.flush();

            String sLine = in.readLine();
            if (sLine != null && sLine.trim().length() > 0) {
                out.println();

                try {
                	if ("q".equalsIgnoreCase(sLine) || "quit".equalsIgnoreCase(sLine)) {
                        CacheFactory.shutdown();
                        out.println(">> all cache services are shut down");
                        break;
                    } else {
                    	String query;
                    	Map params = null;
                    	int pos = sLine.indexOf(';');
                    	if (pos > 0) {
                    		query = sLine.substring(0, pos);
                    		params = parseParams(sLine.substring(pos + 1));
                    	} else {
                    		query = sLine;
                    	}
                        out.println(">> Query is: " + query);
                        
                        Object result;
                        long stamp = System.currentTimeMillis();
                        if (query.startsWith("get ")) {
                        	cacheName = getCacheName(query);
                        	out.println(">> Cache Name: " + cacheName);
                        	Object key = getCacheKey(query);
                        	out.println(">> Cache Key: " + key);
                        	//result = ((QueryCacheClient) client).
                        	result = get(cacheName, key); 
                        } else if (params == null) {
                        	result = client.query(query);
                        } else {
                            out.println(">> Params are: " + params);
                        	result = client.query(query, params);
                        }
                        stamp = System.currentTimeMillis() - stamp;
                        out.println(">> Result is: " + result + "; time taken: " + stamp);
                        
                        if (result != null) {
                            out.println(">> Result type: " + result.getClass().getName());
                            Class[] ints = result.getClass().getInterfaces();
                            if (ints != null && ints.length > 0) {
                            	for (Class cls : ints) {
                                    out.println(">>     Implements: " + cls.getName());
                            	}
                            }
                            if (result instanceof Collection) {
                                out.println(">> Result count: " + ((Collection) result).size());
                            } else if (result instanceof Map) {
                                out.println(">> Result count: " + ((Map) result).size());
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    // LOG the exception and continue
                    out.println(">> Exception during cache operation:");
                    out.println(e);
                    //e.printStackTrace();
                }
            }
        }
    }
    
    private static Map<String, Object> parseParams(String params) {
		StringTokenizer tok = new StringTokenizer(params, ",=");
		Map<String, Object> result = new HashMap<String, Object>();
		while (tok.hasMoreTokens()) {
			String name = tok.nextToken();
			String value = tok.nextToken();
			result.put(name.trim(), value.trim());
		}
		return result.size() > 0 ? result : null;
    }
    
    private static String getCacheName(String query) {
    	int start = query.indexOf('\'');
    	int end = query.indexOf('\'', start + 1);
    	return query.substring(start + 1, end);
    }

    private static Object getCacheKey(String query) {
    	int start = query.indexOf("key()");
    	String key = query.substring(start + 6);
    	start = key.indexOf('=');
    	key = key.substring(start + 1);
    	key = key.trim();
    	if (key.endsWith("'")) {
    		return key.substring(1, key.length() - 1);
    	} else if (key.endsWith("L")) {
    		key = key.substring(0, key.length() - 1);
    		return Long.parseLong(key);
    	}
    	return key;
    }
    
    private static Object get(String cacheName, Object key) {
    	NamedCache cache = CacheFactory.getCache(cacheName);
    	return cache.get(key);
    }

    /**
     *
     * @param query Query string
     * @return Object
     */
    //@Override
	public Object query(String query) {
		return queryCache(query, null);
	}


    /**
     *
     * @param query Query string
     * @param params Parameter map
     * @return Object
     */
    //@Override
	public Object query(String query, Map<String, Object> params) {
		return queryCache(query, params);
	}

    /**
     *
     * @param query Query string
     * @param bindings Binding array
     * @param values Value array
     * @return Object
     */
    //@Override
	public Object queryForSingle(String query, String[] bindings, Object[] values) {
		Map params = new HashMap<String, Object>(bindings.length);
		for (int i=0; i < bindings.length; i++) {
			params.put(bindings[i], values[i]);
		}
		Map result = (Map) queryCache(query, params);
		if (result.size() > 0) {
			Map.Entry e = (Map.Entry) result.entrySet().iterator().next();
			Object value = e.getValue();
			if (result.size() > 1) {
				LOG.trace("got more results then expected: {}", result.size());
			} else {
				LOG.trace("got result: {}::{} ", value.getClass().getName(), value);
			}
			return value;
		} else {
			LOG.trace("got no results for query: {} with params: {}", query, params);
			return null;
		}
	}
	
	private Object queryCache(String query, Map<String, Object> params) {
		
		Object result = null;
        TokenTable toks = CoherenceQueryLanguage.sqlTokenTable();
        query = query + System.getProperty("line.separator");
        SQLOPParser p = new SQLOPParser(query, toks);
        
        Term tn = p.parse();
        CoherenceQuery q = new CoherenceQuery(false);
        q.setExtendedLanguage(false);
        if (q.build((NodeTerm) tn, params)) {
            result = q.execute(); //out, trace);
            LOG.trace("query result: {}", result);
        }
        return result;
	}
	
    
}
