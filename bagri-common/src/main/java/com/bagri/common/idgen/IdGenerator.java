package com.bagri.common.idgen;

public interface IdGenerator<T> {

	boolean adjust(T newValue);
	T next();
	T[] nextRange(int size);
}
