package com.bagri.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.security.LocalSubject;
import com.bagri.common.stats.StatsAggregator;

//import static com.bagri.common.stats.InvocationStatistics.*;

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
//import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A set of static utility methods for JMX
 * 
 * @author Denis Sukhoroslov
 *
 */
public class JMXUtils {

    private static final Logger logger = LoggerFactory.getLogger(JMXUtils.class);
    
	public static final String key_type = "type";
	public static final String key_name = "name";
	public static final String domain = "com.bagri.xdm";
	public static final String type_management = "Management";
    
	/**
	 * Registers system-specific MBean in domain {@code com.bagri.xdm} with type {@code Management} 
	 * 
	 * @param name the MBean name
	 * @param mBean the MBean to register
	 * @return true if MBean was registered, false otherwise
	 */
	public static boolean registerMBean(String name, Object mBean) {
		Hashtable<String, String> keys = getStandardKeys(type_management, name);
		return registerMBean(domain, keys, mBean);
	}

	/**
	 * Registers system-specific MBean in domain {@code com.bagri.xdm}  
	 * 
	 * @param name the MBean name
	 * @param type the MBean type
	 * @param mBean the MBean to register
	 * @return true if MBean was registered, false otherwise
	 */
	public static boolean registerMBean(String type, String name, Object mBean) {
		Hashtable<String, String> keys = getStandardKeys(type, name);
		return registerMBean(domain, keys, mBean);
	}

	/**
	 * Unregisters MBean from JMX repository
	 * 
	 * @param type the MBean type
	 * @param name the MBean name
	 * @return true if MBean was unregistered, false otherwise
	 */
	public static boolean unregisterMBean(String type, String name) {
		Hashtable<String, String> keys = getStandardKeys(type, name);
		return unregisterMBean(domain, keys);
	}
	
	/**
	 * Creates a Hashtable containing type and name MBean properties for futher creation of MBean ObjectName.
	 * 
	 * @param type the MBena type
	 * @param name the MBean name
	 * @return the Hashtable containing the properties above
	 */
	public static Hashtable<String, String> getStandardKeys(String type, String name) {
		Hashtable<String, String> keys = new Hashtable<String, String>(2);
		keys.put(key_type, type);
		keys.put(key_name, name);
		return keys;
	}

	/**
	 * Registers MBean under {@code com.bagri.xdm} domain
	 * 
	 * @param keys the MBean keys to use for registration
	 * @param mBean the MBean to register
	 * @return true if the MBean was registered, false otherwise
	 */
	public static boolean registerMBean(Hashtable<String, String> keys, Object mBean) {
		return registerMBean(domain, keys, mBean);
	}
	
	/**
	 * Produce JMX ObjectName from the MBean type and name provided. Uses {@code com.bagri.xdm} as domain name 
	 * 
	 * @param type the MBean type
	 * @param name the MBean name
	 * @return the MBean ObjectName
	 * @throws MalformedObjectNameException in case of ObjectName construction error
	 */
	public static ObjectName getObjectName(String type, String name) throws MalformedObjectNameException {
		Hashtable<String, String> keys = getStandardKeys(type, name);
		return new ObjectName(domain, keys);
	}
	
	/**
	 * Produce JMX ObjectName from the MBean keys provided. Uses {@code com.bagri.xdm} as domain name
	 * 
	 * @param keys the MBean keys
	 * @return the MBean ObjectName
	 * @throws MalformedObjectNameException in case of ObjectName construction error
	 */
	public static ObjectName getObjectName(Hashtable keys) throws MalformedObjectNameException {
		return new ObjectName(domain, keys);
	}
	
	/**
	 * Produce JMX ObjectName from the MBean keys provided as String. Uses {@code com.bagri.xdm} as domain name
	 * 
	 * @param keys the MBean keys in String format
	 * @return the MBean name
	 * @throws MalformedObjectNameException in case of ObjectName construction error
	 */
	public static ObjectName getObjectName(String keys) throws MalformedObjectNameException {
		return new ObjectName(domain + ":" + keys);
	}

	/**
	 * Registers MBean in JMX repository
	 * 
	 * @param domain the domain to register MBean under
	 * @param keys the MBean keys
	 * @param mBean the MBean
	 * @return true if MBean has been registered successfully, false otherwise
	 */
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
    
	/**
	 * Unregisters MBean from JMX repository. Uses {@code com.bagri.xdm} domain
	 * 
	 * @param keys the MBean keys
	 * @return true if MBean has been unregistered, false otherwise
	 */
	public static boolean unregisterMBean(Hashtable<String, String> keys) {
		return unregisterMBean(domain, keys);
	}

	/**
	 * Unregisters MBean from JMX repository
	 * 
	 * @param domain the domain to unregister MBean from
	 * @param keys the MBean keys
	 * @return true if MBean has been unregistered successfully, false otherwise
	 */
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
	
	/**
	 * Get the current user name connected to JMX server
	 * 
	 * @return the current user name if any, null otherwise
	 */
	public static String getSubjectUser() {
        AccessControlContext ctx = AccessController.getContext();
        Subject subj = Subject.getSubject(ctx);
        String result = null;
        if (subj == null) {
        	subj = LocalSubject.getSubject();
        }
        logger.trace("getSubjectUser; subject: {}", subj);
        if (subj != null) {
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
        logger.trace("getSubjectUser.exit; returning: {}", result);
        return result;
	}
	
	/**
	 * Get the current JMX user name or system user name
	 * 
	 * @return the current JMX user name or system user name. If both are null return "unknown" user name
	 */
	public static String getCurrentUser() {
        String result = getSubjectUser();
        if (result == null) {
        	result = System.getProperty("user.name");
            if (result == null) {
            	result = "unknown";
            }
        }
        logger.trace("getCurrentUser.exit; returning: {}", result);
        return result;
	}

	/**
	 * Get the current JMX user name or login name
	 * 
	 * @param login the default user name
	 * @return the current JMX user name if any, the login name if the current JMX user name is not known
	 */
	public static String getCurrentUser(String login) {
        String result = getSubjectUser();
        return result == null ? login : result;
	}
	
	/**
	 * Query MBean names from JMX server.
	 * 
	 * @param query the JMX query string
	 * @return the list of matching ObjectNames
	 */
	public static List<String> queryNames(String query) {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			 Set<ObjectName> names = mbs.queryNames(new ObjectName(query), null);
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
     * Converts Java Map to JMX CompositeData structure
     * 
     * @param name the produced CompositeData name
     * @param desc the produced CompositeData description
     * @param def the Map to convert
     * @return the produced Composite data
     */
    public static CompositeData mapToComposite(String name, String desc, Map<String, Object> def) {
        if (def != null && !def.isEmpty()) {
            try {
                String[] names = new String[def.size()];
                OpenType[] types = new OpenType[def.size()];
                int idx = 0;
                for (Map.Entry<String, Object> e : def.entrySet()) {
                    names[idx] = e.getKey();
                    types[idx] = getOpenType(e.getValue());
                    idx++;
                }
                CompositeType type = new CompositeType(name, desc, names, names, types);
                return new CompositeDataSupport(type, def);
            } catch (Exception ex) {
                logger.warn("statsToComposite. error: {}", ex.getMessage());
            }
        }
        return null;
    }

    /**
     * Merges CompositeData part with series of another Composite blocks in TabularData format
     * 
     * @param name the series name
     * @param desc the series description
     * @param key the series key
     * @param source the aggregated data to merge with   
     * @param data the series data to be merged into the aggregation result
     * @return the aggregated result in TabularData format
     * @throws OpenDataException in case of data conversion error
     */
    public static TabularData compositeToTabular(String name, String desc, String key, 
    		TabularData source, CompositeData data) throws OpenDataException {
        if (data == null) {
            return source;
        }
        if (source == null) {
        	TabularType type = new TabularType(name, desc, data.getCompositeType(), new String[] {key});
        	source = new TabularDataSupport(type); 
        } 
        source.put(data);
        //logger.trace("getStatisticSeries; added row: {}", data);
        return source;
    }

    /**
     * Aggregates two tabular structures into one. 
     * 
     * @param source the source tabular
     * @param target the target tabular
     * @param aggregator the aggregator which will perform data aggregation 
     * @return the aggregated tabular structure
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TabularData aggregateStats(TabularData source, TabularData target, StatsAggregator aggregator) {
        logger.debug("aggregateStats.enter; got source: {}", source);
        if (source == null) {
        	return target;
        }
		TabularData result = new TabularDataSupport(source.getTabularType());
        Set<List> keys = (Set<List>) source.keySet();
    	if (target == null) {
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

	/**
	 * Aggregates two composite structures into one.
	 * 
	 * @param source the source composite
	 * @param target the target composite
	 * @param aggregator the aggregator which will perform data aggregation
	 * @return the aggregated composite structure
	 */
	public static CompositeData aggregateStats(CompositeData source, CompositeData target, StatsAggregator aggregator) {
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

	/**
	 * Converts set of properties to JMX CompositeData structure
	 * 
	 * @param name the produced CompositeData name
	 * @param desc the produced CompositeData description
	 * @param props the Properties to convert
	 * @return the produced Composite data
	 */
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
     * Converts JMX CompositeData structure to Java Map
     * 
     * @param props the composite structure
     * @return the resulting Java Map
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
     * Converts Map of strings into JMX tabular format. Every name/value pair considered as a separate tab
     * 
     * @param props the source Map
     * @return the resulting JMX tabular structure
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
     * Converts JMX TabularData structure into a Map of strings. Every composite part is should contain name and value parameters 
     * 
     * @param props the source TabularData
     * @return the resulting properties Map
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
     * Converts Java Map into JMX composite format. 
     *  
     * @param props the Map to convert
     * @param compositeType the Composite type to use
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
     * Converts JMX composite structure into a List of strings in "name: value" format
     * 
     * @param data the composite data to convert
     * @return the resulting name/value list
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
     * Converts Map of properties into a List of strings in "name: value" format
     * 
     * @param props the property Map to convert
     * @return the resulting name/value list
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
