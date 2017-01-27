package com.bagri.tools.vvm.service;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;

import com.bagri.tools.vvm.model.*;

import java.util.*;
import java.util.logging.Logger;

public class ClusterServiceProvider implements ClusterManagementService, SchemaManagementService {
    
	private static final Logger LOGGER = Logger.getLogger(ClusterServiceProvider.class.getName());
    private MBeanServerConnection connection;
    
    private Map<String, DocumentManagementService> docMgrs = new HashMap<>();

    public ClusterServiceProvider(MBeanServerConnection connection) {
        this.connection = connection;
    }
    
    public void close() {
    	this.connection = null;
    }

    @Override
    public Node getNode(ObjectName on) throws ServiceException {
        try {
            String name = (String) connection.getAttribute(on, "Name");
            String[] deployedSchemas = new String[0];
            CompositeData optionsCd = null;
            try {
                deployedSchemas = (String[]) connection.getAttribute(on, "DeployedSchemas");
                if (null == deployedSchemas) {
                    deployedSchemas = new String[0];
                }
            } catch (Exception e) {
                LOGGER.throwing(this.getClass().getName(), "getNode.schemas", e);
            }
            try {
                optionsCd = (CompositeData) connection.getAttribute(on, "Options");
            } catch (Exception e) {
                LOGGER.throwing(this.getClass().getName(), "getNode.options", e);
            }
            Node node = new Node(on, name);
            node.setNodeOptions(convertCompositeToNodeOptions(optionsCd));
            node.setDeployedSchemas(Arrays.asList(deployedSchemas));
            return node;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getNode", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public List<Node> getNodes() throws ServiceException {
        try {
            Set<ObjectInstance> instances = connection.queryMBeans(new ObjectName("com.bagri.db:type=Node,name=*"), null);
            List<Node> nodes = new ArrayList<Node>();
            for (ObjectInstance instance : instances) {
                Node node = getNode(instance.getObjectName());
                nodes.add(node);
            }
            return nodes;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getNodes", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void saveNode(Node node) throws ServiceException {
        try {
            // TODO: What da hell, refactor it!!!. Define equals for NodeOption or inherit NodeOptions from Properties
            List<NodeOption> existing = convertCompositeToNodeOptions((CompositeData) connection.getAttribute(node.getObjectName(), "Options"));
            List<String> toDelete = new ArrayList<String>();
            for (NodeOption option : existing) {
                String key = option.getOptionName();
                boolean found = false;
                for (NodeOption newOption : node.getNodeOptions()) {
                    if (key.equals(newOption.getOptionName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    toDelete.add(key);
                }
            }
            for (String key : toDelete) {
                connection.invoke(node.getObjectName()
                        , "removeOption"
                        , new Object[] {key}
                        , new String[] {String.class.getName()});
            }
            for (NodeOption option : node.getNodeOptions()) {
                connection.invoke(node.getObjectName()
                        , "setOption", new Object[] {option.getOptionName()
                        , option.getOptionValue()}
                        , new String[]{String.class.getName()
                        , String.class.getName()});
            }
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "saveNode", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void addNode(Node node) throws ServiceException {
        String optionsStr = convertOptionsToString(node.getNodeOptions());
        try {
            connection.invoke(new ObjectName("com.bagri.db:type=Management,name=ClusterManagement")
                    , "addNode"
                    , new Object[] {node.getName(), optionsStr}
                    , new String[] {String.class.getName(), String.class.getName()});
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "addNode", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void deleteNode(Node node) throws ServiceException {
        try {
            connection.invoke(new ObjectName("com.bagri.db:type=Management,name=ClusterManagement")
                    , "deleteNode"
                    , new Object[] {node.getName()}
                    , new String[] {String.class.getName()});
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "deleteNode", e);
            throw new ServiceException(e);
        }
    }

	@Override
	public DocumentManagementService getDocumentManagement(String schemaName) {
		DocumentManagementService docMgr = docMgrs.get(schemaName);
		if (docMgr == null) {
			docMgr = new DocumentServiceProvider(connection, schemaName);
			docMgrs.put(schemaName, docMgr);
		}
		return docMgr;
	}

    @Override
    public List<Schema> getSchemas() throws ServiceException {
        try {
            Set<ObjectInstance> instances = connection.queryMBeans(new ObjectName("com.bagri.db:type=Schema,name=*"), null);
            List<Schema> schemas = new ArrayList<Schema>();
            for (ObjectInstance instance : instances) {
                Schema schema = extractSchema(instance);
                schemas.add(schema);
            }
            return schemas;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getSchemas", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public Properties getDefaultProperties() throws ServiceException {
        try {
            CompositeData cd = (CompositeData) connection.invoke(new ObjectName("com.bagri.db:type=Management,name=SchemaManagement")
                    , "getDefaultProperties"
                    , null
                    , null);
            return convertCompositeToProperties(cd);
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getDefaultProperties", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void setDefaultProperty(Property property) throws ServiceException {
        try {
            connection.invoke(new ObjectName("com.bagri.db:type=Management,name=SchemaManagement")
                    , "setDefaultProperty"
                    , new Object[] {property.getPropertyName(), property.getPropertyValue()}
                    , new String[] {String.class.getName(), String.class.getName()});
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "setDefaultProperty", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void addSchema(Schema schema) throws ServiceException {
        try {
            connection.invoke(new ObjectName("com.bagri.db:type=Management,name=SchemaManagement")
                    , "createSchema"
                    , new Object[] {schema.getSchemaName(), schema.getDescription(), convertPropertiesToString(schema.getProperties())}
                    , new String[] {String.class.getName(), String.class.getName(), String.class.getName()});
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "addSchema", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public Schema getSchema(String schemaName) throws ServiceException {
        try {
            ObjectInstance oi = connection.getObjectInstance(new ObjectName("com.bagri.db:type=Schema,name=" + schemaName));
            if (null != oi) {
                return extractSchema(oi);
            }
            return null;
        } catch (InstanceNotFoundException e) {
        	return null;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getSchema", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public Schema getSchema(ObjectName objectName) throws ServiceException {
        try {
            ObjectInstance oi = connection.getObjectInstance(objectName);
            if (null != oi) {
                return extractSchema(oi);
            }
            return null;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getSchema", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void saveSchema(Schema schema) throws ServiceException {
        try {
            if (null != schema.getDescription()) {
                connection.invoke(schema.getObjectName()
                        , "setDescription"
                        , new Object[] {schema.getDescription()}
                        , new String[] {String.class.getName()});
            }
            if (schema.isActive()) {
                connection.invoke(schema.getObjectName()
                        , "activateSchema"
                        , null
                        , null);
            } else {
                connection.invoke(schema.getObjectName()
                        , "deactivateSchema"
                        , null
                        , null);
            }
            Properties existing = convertCompositeToProperties((CompositeData) connection.invoke(schema.getObjectName(), "getProperties", null, null));
            existing.keySet().removeAll(schema.getProperties().keySet());
            Set<Object> toDelete = existing.keySet();
            for (Object key : toDelete) {
                connection.invoke(schema.getObjectName()
                        , "removeProperty"
                        , new Object[] {key.toString()}
                        , new String[] {String.class.getName()});
            }
            for (Object key : schema.getProperties().keySet()) {
                connection.invoke(schema.getObjectName()
                        , "setProperty"
                        , new Object[] {key.toString(), schema.getProperties().getProperty(key.toString())}
                        , new String[]{String.class.getName(), String.class.getName()});
            }
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "saveSchema", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void deleteSchema(Schema schema) throws ServiceException {
        try {
            connection.invoke(new ObjectName("com.bagri.db:type=Management,name=SchemaManagement")
                    , "destroySchema"
                    , new Object[] {schema.getSchemaName()}
                    , new String[] {String.class.getName()});
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "deleteSchema", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public void cancelQuery(String schemaName) throws ServiceException {
        try {
            Object vars = connection.invoke(getSchemaObjectName("QueryManagement", schemaName)
                    , "cancelQuery"
                    , null
                    , null
            );
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "cancelQuery", e);
            throw new ServiceException(e);
        }
    }
    
    @Override
    public List<String> parseQuery(String schemaName, String query, Properties props) throws ServiceException {
        try {
            Object vars = connection.invoke(getSchemaObjectName("QueryManagement", schemaName)
                    , "parseQuery"
                    , new Object[] {query, props}
                    , new String[] {String.class.getName(), Properties.class.getName()}
            );
            return Arrays.asList((String[]) vars);
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "parseQuery", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public Object runQuery(String schemaName, boolean direct, String query, Map<String, Object> params, Properties props) throws ServiceException {
        try {
        	CompositeData bindings = null;
        	if (params != null) {
        		bindings = mapToComposite("param", "desc", params);
        	}
            Object res = connection.invoke(getSchemaObjectName("QueryManagement", schemaName)
                    , "runPreparedQuery"
                    , new Object[] {query, direct, bindings, props}
                    , new String[] {String.class.getName(), boolean.class.getName(), CompositeData.class.getName(), Properties.class.getName()}
            );
            return res;
        } catch (Throwable e) {
            LOGGER.throwing(this.getClass().getName(), "runQuery", e);
            throw new ServiceException(e);
        }
    }

    @Override
    public Properties getQueryProperties(String schemaName) throws ServiceException {
    	Properties props = new Properties();
    	try {
    		ObjectName on = getSchemaObjectName("QueryManagement", schemaName);
    		Object value = connection.getAttribute(on, "FetchSize");
    		props.setProperty("bdb.client.fetchSize", value.toString());
    		value = connection.getAttribute(on, "QueryTimeout");
    		props.setProperty("xqj.schema.queryTimeout", value.toString());
    		return props;
    	} catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "getQueryProperties", ex);
            throw new ServiceException(ex);
    	}
    }
    
    @Override
    public long[] getSchemaVolumeStatistics(String schemaName) throws ServiceException {

        try {
            Object res = connection.getAttribute(getSchemaObjectName("DocumentManagement", schemaName), "TotalCounts");
            CompositeData cd = (CompositeData) res;
            return new long[] {(Integer) cd.get("Number of documents"), (Integer) cd.get("Number of elements"), (Long) cd.get("Consumed size")};
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getSchemaVolumeStatistics", e);
            throw new ServiceException(e);
        }
    }
    
    @Override
    public long[] getSchemaTransactionStatistics(String schemaName) throws ServiceException {
    	
        try {
            Object res = connection.getAttribute(getSchemaObjectName("TransactionManagement", schemaName), "TxStatistics");
            CompositeData cd = (CompositeData) res;
            return new long[] {(Long) cd.get("Started"), (Long) cd.get("In Progress"), (Long) cd.get("Commited"), (Long) cd.get("Rolled Back")};
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getSchemaTransactionStatistics", e);
            throw new ServiceException(e);
        }
    	
    }
    
    @Override
    public TabularData getSchemaPartitionStatistics(String schemaName) throws ServiceException {

    	int flag = 0;
        try {
        	TabularData result = (TabularData) connection.invoke(getSchemaObjectName("DocumentManagement", schemaName), 
            		"getPartitionStatistics", new Object[] {flag}, new String[] {int.class.getName()});
            //LOGGER.info("getPartitionStatistics; got results: " + result.size());
        	return result;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getSchemaPartitionStatistics", e);
            throw new ServiceException(e);
        }
    }
    
    @Override
    public List<String> getWorkingHosts(String schemaName) throws ServiceException {
    	
        try {
            Object res = connection.getAttribute(new ObjectName("com.bagri.db:type=Schema,name=" + schemaName), "ActiveNodes");
            return Arrays.asList((String[]) res);
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getWorkingHosts", e);
            throw new ServiceException(e);
        }
    }
    
    private ObjectName getSchemaObjectName(String kind, String schemaName) throws MalformedObjectNameException {
    	return new ObjectName("com.bagri.db:type=Schema,kind=" + kind + ",name=" + schemaName);
    }

    private Schema extractSchema(ObjectInstance oi) {
        ObjectName on = oi.getObjectName();
        String name = null;
        String description = null;
        String dataFormat =  null;
        String state = null;
        boolean active = false;
        boolean persistent = false;
        int version = -1;
        String[] registeredTypes = new String[0];
        CompositeData propertiesCd = null;

        try {
            name = (String) connection.getAttribute(on, "Name");
        } catch (Exception e) {/* Ignore it for now */}
        try {
            description = (String) connection.getAttribute(on, "Description");
        } catch (Exception e) {/* Ignore it for now */}
        try {
            dataFormat = (String) connection.getAttribute(on, "DataFormat");
        } catch (Exception e) {/* Ignore it for now */}
        try {
            state = (String) connection.getAttribute(on, "State");
        } catch (Exception e) {/* Ignore it for now */}
        try {
            active = (Boolean) connection.getAttribute(on, "Active");
        } catch (Exception e) {/* Ignore it for now */}
        try {
            persistent = (Boolean) connection.getAttribute(on, "Persistent");
        } catch (Exception e) {/* Ignore it for now */}
        try {
            version = (Integer) connection.getAttribute(on, "Version");
        } catch (Exception e) {/* Ignore it for now */}
        try {
            registeredTypes = (String[]) connection.getAttribute(on, "RegisteredTypes");
        } catch (Exception e) {/* Ignore it for now */}
        try {
            propertiesCd = (CompositeData) connection.getAttribute(on, "Properties");
        } catch (Exception e) {/* Ignore it for now */}

        Schema schema = new Schema(name);
        schema.setObjectName(on);
        schema.setDescription(description);
        schema.setDataFormat(dataFormat);
        schema.setState(state);
        schema.setActive(active);
        schema.setPersistent(persistent);
        schema.setVersion(version);
        schema.setRegisteredTypes(registeredTypes);
        schema.setProperties(convertCompositeToProperties(propertiesCd));
        return schema;
    }

    private List<NodeOption> convertCompositeToNodeOptions(CompositeData cd) {
        List<NodeOption> options = new ArrayList<NodeOption>();
        if (null == cd) {
            return options;
        }
        Set<String> keys = cd.getCompositeType().keySet();
        for (String key : keys) {
            String value = (String) cd.get(key);
            NodeOption option = new NodeOption(key, value);
            options.add(option);
        }
        return options;
    }

    private String convertOptionsToString(List<NodeOption> options) {
        String result = "";
        for (NodeOption o : options) {
            result += o.getOptionName() + "=" + o.getOptionValue() + ";";
        }
        return result;
    }

    private String convertPropertiesToString(Properties properties) {
        String result = "";
        for (Object key : properties.keySet()) {
            result += key + "=" + properties.get(key) + ";";
        }
        return result;
    }

    private Properties convertCompositeToProperties(CompositeData cd) {
        Properties properties = new Properties();
        if (null == cd) {
            return properties;
        }
        Set<String> keys = cd.getCompositeType().keySet();
        for (String key : keys) {
            String value = (String) cd.get(key);
            properties.put(key, value);
        }
        return properties;
    }

    private CompositeData mapToComposite(String name, String desc, Map<String, Object> map) {
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
                //logger.warn("statsToComposite. error: {}", ex.getMessage());
            }
        }
        return null;
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
