package com.bagri.core.system;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents an action to be performed when trigger fires
 * 
 * @author Denis Sukhoroslov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://www.bagridb.com/schema/system", propOrder = {
		"order",
		"scope"
})
public class TriggerAction {

	/**
	 * Represents action ordering: will it be performed before operation happens or right after it happens. 
	 */
	@XmlType(name = "Order", namespace = "http://www.bagridb.com/schema/system")
	@XmlEnum
	public enum Order {

		/**
		 * before operation happens
		 */
	    @XmlEnumValue("before")
		before,

		/**
		 * after operation happens
		 */
	    @XmlEnumValue("after")
		after
	}
	
	/**
	 * Represents action scope: the point in system lifecycle when action has to be performed
	 */
	@XmlType(name = "Scope", namespace = "http://www.bagridb.com/schema/system")
	@XmlEnum
	public enum Scope {

		/**
		 * document inserted 
		 */
	    @XmlEnumValue("insert")
		insert,

		/**
		 * document updated (versioned)
		 */
	    @XmlEnumValue("update")
		update,

		/**
		 * document deleted
		 */
	    @XmlEnumValue("delete")
		delete,

		/**
		 * transaction started
		 */
	    @XmlEnumValue("begin")
		begin,

		/**
		 * transaction commited
		 */
		@XmlEnumValue("commit")
		commit,
		
		/**
		 * transaction rolled back
		 */
	    @XmlEnumValue("rollback")
		rollback
		
	}
	
	@XmlAttribute(required = true)
	private Scope scope;
	
	@XmlAttribute(required = true)
	private Order order;
	
	/**
	 * default constructor
	 */
	public TriggerAction() {
		// for JAXB
	}

	/**
	 * 
	 * @param order the action order
	 * @param scope the action scope
	 */
	public TriggerAction(Order order, Scope scope) {
		this.order = order;
		this.scope = scope;
	}

	/**
	 * 
	 * @return the action order
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * 
	 * @return the action scope
	 */
	public Scope getScope() {
		return scope;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = 1;
		result = 31 + order.ordinal();
		result = 31 * result + scope.ordinal();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TriggerAction other = (TriggerAction) obj;
		if (order != other.order) {
			return false;
		}
		if (scope != other.scope) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return order + " " + scope;
	}

	
}
