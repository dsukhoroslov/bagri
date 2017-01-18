package com.bagri.core.query;

/**
 * Possible comparisons enumeration. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public enum Comparison {
	
	/**
	 * equals
	 */
	EQ,
	
	/**
	 * not equals
	 */
	NE,
	
	/**
	 * less
	 */
	LT,
	
	/**
	 * less or equals
	 */
	LE,
	
	/**
	 * greater
	 */
	GT,
	
	/**
	 * greater or equals
	 */
	GE,
	
	/**
	 * in 
	 */
	IN,
	
	/**
	 * like
	 */
	LIKE,
	
	/**
	 * between
	 */
	BETWEEN,
	
	/**
	 * and
	 */
	AND,
	
	/**
	 * or
	 */
	OR,
	
	/**
	 * not
	 */
	NOT;
	
	/**
	 * Negates the provided Comparison value
	 * 
	 * @param comp the Comparison value
	 * @return negated Comparison value
	 */
	public static Comparison negate(Comparison comp) {
		switch (comp) {
			case EQ: return Comparison.NE;
			case NE: return Comparison.EQ;
			case LE: return Comparison.GT;
			case LT: return Comparison.GE;
			case GE: return Comparison.LT;
			case GT: return Comparison.LE;
			default: return comp;
		}
	}
	
	/**
	 * Reverses the provided Comparison value
	 * 
	 * @param comp the Comparison value
	 * @return reverted Comparison value 
	 */
	public static Comparison revert(Comparison comp) {
		switch (comp) {
			case EQ: return Comparison.EQ;
			case NE: return Comparison.NE;
			case LE: return Comparison.GE; 
			case LT: return Comparison.GT; 
			case GE: return Comparison.LE; 
			case GT: return Comparison.LT; 
			default: return comp;
		}
	}
	
	/**
	 * Check is the provided Comparison value implies Binary expression or not.
	 * 
	 * @param compType the Comparison value 
	 * @return true if compType is AND or OR, false otherwise
	 */
	public static boolean isBinary(Comparison compType) {
		switch (compType) {
			case AND:
			case OR: return true;
			//case NOT:
			//	return -1;
			default: return false;
		}
	}

}
