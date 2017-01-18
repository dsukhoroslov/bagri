package com.bagri.server.hazelcast.predicate;

import com.bagri.core.DocumentKey;
import com.bagri.core.model.Document;
import com.bagri.core.query.Comparison;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentPredicateBuilder {
	
	@SuppressWarnings("unchecked")
	public static Predicate<DocumentKey, Document> getQuery(String pattern) {
		String[] parts = pattern.split(",");
		Predicate<DocumentKey, Document> result = null;
		for (String part: parts) {
			//logger.trace("getDocumentUris; translating query part: {}", part);
			Predicate<DocumentKey, Document> query = toPredicate(part.trim());
			if (query != null) {
				if (result == null) {
					result = query;
				} else {
					result = Predicates.and(result, query);
				}
			} else {
			//	logger.info("getDocumentUris; cannot translate query part '{}' to Predicate, skipping", part);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static Predicate<DocumentKey, Document> toPredicate(String query) {
		int pos = query.indexOf(" ");
		if (pos > 0) {
			String attr = query.substring(0, pos); 
			int pos2 = query.indexOf(" ", pos + 1);
			if (pos2 > 0) {
				Comparison comp = toComparison(query.substring(pos, pos2).trim());
				if (comp != null) {
					String val = query.substring(pos2);
					Comparable<?> value = toValue(attr, val.trim());
					//logger.trace("toPredicate; got predicate parts: {} {} {}", attr, comp, value);
					switch (comp) {
						case EQ: return Predicates.equal(attr, value);
						case NE: return Predicates.notEqual(attr, value);
						case GT: return Predicates.greaterThan(attr, value);
						case GE: return Predicates.greaterEqual(attr, value);
						case LT: return Predicates.lessThan(attr, value);
						case LE: return Predicates.lessEqual(attr, value);
						case LIKE: return Predicates.like(attr, value.toString());
					}
				}
			}
		}
		return null;
	}
		
	private static Comparison toComparison(String comp) {
		switch (comp) {
			case "=": return Comparison.EQ;
			case "!=": return Comparison.NE;
			case "<": return Comparison.LT;
			case "<=": return Comparison.LE;
			case ">": return Comparison.GT;
			case ">=": return Comparison.GE;
			case "not": return Comparison.NOT;
			case "like": return Comparison.LIKE;
			case "between": return Comparison.BETWEEN;
			case "and": return Comparison.AND;
			case "or": return Comparison.OR;
		}
		return null;
	}

	private static Comparable<?> toValue(String attr, String value) {
		switch (attr) {
		    //case "key": 
		    case "version": return new Integer(value); 
		    case "uri": return value;
		    //case "type"
		    //case "encoding": 
		    case "txStart": 
		    case "txFinish": return new Long(value);
		    case "createdAt": return new Long(value); //Date()
		    case "createdBy": return value;
		    case "bytes": 
		    case "elements": return new Integer(value);
		}
		return value;
	}
	
}
