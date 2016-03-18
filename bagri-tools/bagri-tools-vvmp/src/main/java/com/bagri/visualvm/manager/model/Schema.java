package com.bagri.visualvm.manager.model;

import javax.management.ObjectName;
import java.util.Properties;

public class Schema implements Comparable<Schema> {
	
    private ObjectName objectName;
    private String schemaName;
    private String description;
    private String persistenceType;
    private String state;
    private boolean active;
    private int version;
    private String[] registeredTypes;
    private Properties properties;

    public Schema(String schemaName) {
        this.schemaName = schemaName;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPersistenceType() {
        return persistenceType;
    }

    public void setPersistenceType(String persistenceType) {
        this.persistenceType = persistenceType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String[] getRegisteredTypes() {
        return registeredTypes;
    }

    public void setRegisteredTypes(String[] registeredTypes) {
        this.registeredTypes = registeredTypes;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return schemaName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schema schema = (Schema) o;

        if (!schemaName.equals(schema.schemaName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return schemaName.hashCode();
    }

	@Override
	public int compareTo(Schema other) {
		return schemaName.compareTo(other.schemaName);
	}
}
