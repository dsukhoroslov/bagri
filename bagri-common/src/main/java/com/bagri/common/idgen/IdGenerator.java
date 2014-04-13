package com.bagri.common.idgen;

public interface IdGenerator<T> {

	T next();
	T[] nextRange(int size);
}
