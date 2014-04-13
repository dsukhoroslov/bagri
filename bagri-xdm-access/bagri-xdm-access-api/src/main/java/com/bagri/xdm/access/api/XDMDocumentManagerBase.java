package com.bagri.xdm.access.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.query.BinaryExpression;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.Expression;
import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.PathExpression;
import com.bagri.xdm.common.XDMFactory;

public abstract class XDMDocumentManagerBase {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	protected XDMFactory mFactory;
	protected XDMSchemaDictionary mDictionary;
	
	public XDMFactory getXdmFactory() {
		return this.mFactory;
	}
	
	public void setXdmFactory(XDMFactory factory) {
		this.mFactory = factory;
	}
	
	//@Override
	public XDMSchemaDictionary getSchemaDictionary() {
		return this.mDictionary;
	}
	
	public void setSchemaDictionary(XDMSchemaDictionary dictionary) {
		this.mDictionary = dictionary;
	}

	protected abstract Set<Long> queryPathKeys(Set<Long> found, PathExpression pex);
	public abstract Collection<Long> getDocumentIDs(ExpressionBuilder query);
	
	public Set<Long> queryKeys(Set<Long> found, Expression ex) {
		if (ex instanceof BinaryExpression) {
			BinaryExpression be = (BinaryExpression) ex;
			Set<Long> leftKeys = queryKeys(found, be.getLeft());
			if (Comparison.AND.equals(be.getCompType())) {
				if (leftKeys.size() == 0) {
					return leftKeys;
				}
				Set<Long> rightKeys = queryKeys(leftKeys, be.getRight());
				return rightKeys;
			} else if (Comparison.OR.equals(be.getCompType())) {
				Set<Long> rightKeys = queryKeys(found, be.getRight());
				leftKeys.addAll(rightKeys);
				return leftKeys;
			} else {
				throw new IllegalArgumentException("Wrong BinaryExpression type: " + be.getCompType());
			}
		}
		
		return queryPathKeys(found, (PathExpression) ex);
	}
	
	//@Override
	public Collection<String> getDocumentURIs(ExpressionBuilder query) {
		Collection<Long> keys = getDocumentIDs(query);
		Collection<String> result = new ArrayList<String>();
		for (Long key: keys) {
			result.add(String.valueOf(key));
		}
		return result;
	}
	
}
