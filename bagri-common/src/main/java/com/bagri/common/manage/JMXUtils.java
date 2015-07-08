package com.bagri.common.manage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.security.LocalSubject;

import static com.bagri.common.stats.InvocationStatistics.*;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;

import java.lang.management.ManagementFactory;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * JMX Utils class
 * Date: 2/7/13 5:10 PM
 */
public class JMXUtils {

    private static final Logger logger = LoggerFactory.getLogger(JMXUtils.class);
    
	public static final String key_type = "type";
	public static final String key_name = "name";
	public static final String domain = "com.bagri.xdm";
	public static final String type_management = "Management";
    
	public static boolean registerMBean(String name, Object mBean) {
		Hashtable<String, String> keys = getStandardKeys(type_management, name);
		return registerMBean(domain, keys, mBean);
	}

	public static boolean registerMBean(String type, String name, Object mBean) {
		Hashtable<String, String> keys = getStandardKeys(type, name);
		return registerMBean(domain, keys, mBean);
	}

	public static boolean unregisterMBean(String type, String name) {
		Hashtable<String, String> keys = getStandardKeys(type, name);
		return unregisterMBean(domain, keys);
	}
	
	public static Hashtable<String, String> getStandardKeys(String type, String name) {
		Hashtable<String, String> keys = new Hashtable<String, String>(2);
		keys.put(key_type, type);
		keys.put(key_name, name);
		return keys;
	}

	public static boolean registerMBean(Hashtable<String, String> keys, Object mBean) {
		return registerMBean(domain, keys, mBean);
	}
	
	public static ObjectName getObjectName(String type, String name) throws MalformedObjectNameException {
		Hashtable<String, String> keys = getStandardKeys(type, name);
		return new ObjectName(domain, keys);
	}
	
	public static ObjectName getObjectName(Hashtable keys) throws MalformedObjectNameException {
		return new ObjectName(domain, keys);
	}
	
	public static ObjectName getObjectName(String keys) throws MalformedObjectNameException {
		return new ObjectName(domain + ":" + keys);
	}

	public static boolean registerMBean(String domain, Hashtable<String, String> keys, Object mBean) {
		
		ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
		MBeanServer defServer = servers.get(0);
		ObjectName name;
		try {
			name = new ObjectName(domain, keys);
			defServer.registerMBean(mBean, name);
			return true;
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | 
				MBeanRegistrationException | NotCompliantMBeanException ex) {
			logger.error("register.error: " + ex.getMessage(), ex);
		}
		return false;
	}
    
	public static boolean unregisterMBean(Hashtable<String, String> keys) {
		return unregisterMBean(domain, keys);
	}

	public static boolean unregisterMBean(String domain, Hashtable<String, String> keys) {
		
		ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
		MBeanServer defServer = servers.get(0);
		ObjectName name;
		try {
			name = new ObjectName(domain, keys);
			defServer.unregisterMBean(name);
			return true;
		} catch (MalformedObjectNameException |  MBeanRegistrationException | InstanceNotFoundException ex) {
			logger.error("unregister.error: " + ex.getMessage(), ex);
		}
		return false;
	}
	
	public static String getCurrentUser() {
        AccessControlContext ctx = AccessController.getContext();
        Subject subj = Subject.getSubject(ctx);
        String result = null;
        if (subj == null) {
        	subj = LocalSubject.getSubject();
        }
        logger.trace("getCurrentUser; subject: {}", subj);
        if (subj == null) {
        	result = System.getProperty("user.name");
        } else {
	        Set<JMXPrincipal> sjp = subj.getPrincipals(JMXPrincipal.class);
	        if (sjp != null && sjp.size() > 0) {
	        	result = sjp.iterator().next().getName();
	        } else {
		        Set<Principal> sp = subj.getPrincipals();
		        if (sp != null && sp.size() > 0) {
		        	result = sp.iterator().next().getName();
		        }
	        }
        }
        if (result == null) {
        	result = "unknown";
        }
        logger.trace("getCurrentUser.exit; returning: {}", result);
        return result;
	}
	
	public static List<String> queryNames(String wildcard) {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			 Set<ObjectName> names = mbs.queryNames(new ObjectName(wildcard), null);
			 List<String> result = new ArrayList<String>(names.size());
			 for (ObjectName name: names) {
				 result.add(name.toString());
			 }
			 return result; 
		} catch (MalformedObjectNameException ex) {
			logger.error("queryNames.error: " + ex.getMessage(), ex);
		}
		return Collections.emptyList();
	}
    
    /**
     * @param name  Statistics name
     * @param desc  Statistics description
     * @param map	Statistics name/value map
     * @return Composite data
     */
    public static CompositeData mapToComposite(String name, String desc, Map<String, Object> map) {
        if (map != null && !map.isEmpty()) {
            try {
                String[] names = new String[map.size()];
                OpenType[] types = new OpenType[map.size()];
                int idx = 0;
                for (Map.Entry<String, Object> e : map.entrySet()) {
                    names[idx] = e.getKey();
                    types[idx] = getOpenType(e.getValue());
                    idx++;
                }
                CompositeType type = new CompositeType(name, desc, names, names, types);
                return new CompositeDataSupport(type, map);
            } catch (OpenDataException ex) {
                logger.warn("statsToComposite. error: {}", ex.getMessage());
            }
        }
        return null;
    }

    public static TabularData compositeToTabular(String name, String desc, String key, 
    		TabularData source, CompositeData data) throws OpenDataException {
        if (data == null) {
            return source;
        }
        if (source == null) {
        	TabularType type = new TabularType(name, desc, data.getCompositeType(),	new String[] {key});
        	source = new TabularDataSupport(type); 
        } 
        source.put(data);
        //logger.trace("getStatisticSeries; added row: {}", data);
        return source;
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TabularData aggregateStats(TabularData source, TabularData target, StatsAggregator aggregator) {
    	// source is not nullable
        logger.debug("aggregateStats.enter; got source: {}", source);
        if (source == null) {
        	return target;
        }
		TabularData result = new TabularDataSupport(source.getTabularType());
        Set<List> keys = (Set<List>) source.keySet();
    	if (target == null) {
       		//for (List key: keys) {
       		//	result.put(source.get(key.toArray()));
        	//}
    		return source;
    	} else {
       		for (List key: keys) {
       			Object[] index = key.toArray();
       			CompositeData aggr = aggregateStats(source.get(index), target.get(index), aggregator);
       			result.put(aggr);
        	}
    	}
        logger.debug("aggregateStats.exit; returning: {}", result);
		return result;
	}
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static CompositeData aggregateStats(CompositeData source, CompositeData target, StatsAggregator aggregator) {
    	// source is not nullable
		Set<String> keys = source.getCompositeType().keySet();
		String[] names = keys.toArray(new String[keys.size()]);
		Object[] srcVals = source.getAll(names);
    	if (target == null) {
    		try {
				target = new CompositeDataSupport(source.getCompositeType(), names, srcVals);
			} catch (OpenDataException ex) {
                logger.warn("aggregateStats. error: {}", ex.getMessage());
                return null;
			}
    	} else {
    		Object[] trgVals = aggregator.aggregateStats(srcVals, target.getAll(names));
			try {
				target = new CompositeDataSupport(source.getCompositeType(), names, trgVals);
			} catch (OpenDataException ex) {
                logger.warn("aggregateStats. error: {}", ex.getMessage());
                return null;
			}
    	}
    	return target;
    }
    
    public static CompositeData propsToComposite(String name, String desc, Properties props) {
    	logger.trace("propsToComposite; name: {}; properties: {}", name, props);
        if (props != null && !props.isEmpty()) {
            try {
                String[] names = new String[props.size()];
                OpenType[] types = new OpenType[props.size()];
                Object[] values = new Object[props.size()];
                int idx = 0;
                for (Map.Entry<Object, Object> e : props.entrySet()) {
                    names[idx] = (String) e.getKey();
                    types[idx] = getOpenType(e.getValue());
                    values[idx] = e.getValue();
                    idx++;
                }
                CompositeType type = new CompositeType(name, desc, names, names, types);
                return new CompositeDataSupport(type, names, values);
            } catch (/*OpenDataException ex*/ Throwable ex) {
                logger.warn("propsToComposite. error: {}", ex.getMessage());
            }
        }
        return null;
    }

    /**
     * @param props Property map
     * @return Composite data
     */
    public static Map<String, Object> propsFromComposite(CompositeData props) {
        Map<String, Object> result = new HashMap<String, Object>(props.values().size());
        CompositeType type = props.getCompositeType();
        for (String name : type.keySet()) {
            result.put(name, props.get(name));
        }
        return result;
    }

    /**
     * @param props Property map
     * @return Tabular data
     */
    public static TabularData propsToTabular(Map<String, String> props) {
        TabularData result = null;
        try {
            String typeName = "java.util.Map<java.lang.String, java.lang.String>";
            String[] nameValue = new String[]{"name", "value"};
            OpenType[] openTypes = new OpenType[]{SimpleType.STRING, SimpleType.STRING};
            CompositeType rowType = new CompositeType(typeName, typeName, nameValue, nameValue, openTypes);
            TabularType tabularType = new TabularType(typeName, typeName, rowType, new String[]{"name"});
            result = new TabularDataSupport(tabularType);
            if (props != null && props.size() > 0) {
                for (Map.Entry<String, String> prop : props.entrySet()) {
                    result.put(new CompositeDataSupport(rowType, nameValue, new Object[]{prop.getKey(), prop.getValue()}));
                }
            }
        } catch (OpenDataException ex) {
            logger.error("propsToTabular. error: ", ex);
        }
        return result;
    }

    /**
     * @param props Tabular data
     * @return Property map
     */
    public static Map<String, String> propsFromTabular(TabularData props) {
        Map<String, String> result = new HashMap<String, String>(props.size());
        for (Object prop : props.values()) {
            CompositeData data = (CompositeData) prop;
            result.put((String) data.get("name"), (String) data.get("value"));
        }
        return result;
    }

    /**
     * @param props         Property map
     * @param compositeType Composite type
     * @return CompositeData based on type and property Map
     */
    public static CompositeData propsToComposite(Map<String, Object> props, CompositeType compositeType) {
        CompositeData result = null;
        try {
            result = new CompositeDataSupport(compositeType, props);
        } catch (OpenDataException ex) {
            logger.error("propsToComposite. error: ", ex);
        }
        return result;
    }


    /**
     * @param data Composite data
     * @return EOD statistics
     */
    public static List<String> compositeToStrings(CompositeData data) {
        Set<String> keys = data.getCompositeType().keySet();
        List<String> result = new ArrayList<String>(keys.size());
        for (String key : keys) {
            result.add(key + ": " + data.get(key));
        }
        return result;
    }


    /**
     * @param props Property map
     * @return EOD statistics
     */
    public static List<String> propsToStrings(Map<String, Object> props) {
        List<String> result = new ArrayList<String>(props.size());
        for (Map.Entry<String, Object> e : props.entrySet()) {
            result.add(e.getKey() + ": " + e.getValue());
        }
        return result;
    }

    private static OpenType getOpenType(Object value) {
        if (value == null) {
            return SimpleType.VOID;
        }
        String name = value.getClass().getName();
        //if (OpenType.ALLOWED_CLASSNAMES_LIST.contains(name)) {

        if ("java.lang.Long".equals(name)) {
            return SimpleType.LONG;
        }
        if ("java.lang.Integer".equals(name)) {
            return SimpleType.INTEGER;
        }
        if ("java.lang.String".equals(name)) {
            return SimpleType.STRING;
        }
        if ("java.lang.Double".equals(name)) {
            return SimpleType.DOUBLE;
        }
        if ("java.lang.Float".equals(name)) {
            return SimpleType.FLOAT;
        }
        if ("java.math.BigDecimal".equals(name)) {
            return SimpleType.BIGDECIMAL;
        }
        if ("java.math.BigInteger".equals(name)) {
            return SimpleType.BIGINTEGER;
        }
        if ("java.lang.Boolean".equals(name)) {
            return SimpleType.BOOLEAN;
        }
        if ("java.lang.Byte".equals(name)) {
            return SimpleType.BYTE;
        }
        if ("java.lang.Character".equals(name)) {
            return SimpleType.CHARACTER;
        }
        if ("java.util.Date".equals(name)) {
            return SimpleType.DATE;
        }
        if ("java.lang.Short".equals(name)) {
            return SimpleType.SHORT;
        }
        //"javax.management.ObjectName",
        //CompositeData.class.getName(),
        //TabularData.class.getName()
        //}
        return null; // is it allowed ??
    }
}
