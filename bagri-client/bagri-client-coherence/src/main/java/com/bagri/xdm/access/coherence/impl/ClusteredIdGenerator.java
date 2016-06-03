package com.bagri.xdm.access.coherence.impl;

import com.bagri.common.idgen.IdGenerator;
import com.oracle.coherence.common.ranges.Range;
import com.oracle.coherence.common.sequencegenerators.SequenceGenerator;

public class ClusteredIdGenerator implements IdGenerator<Long> {
	
	private SequenceGenerator sqGen;
	
	public ClusteredIdGenerator(SequenceGenerator sqGen) {
		this.sqGen = sqGen;
	}

	@Override
	public Long next() {
		return sqGen.next();
	}

	@Override
	public Long[] nextRange(int size) {
		Range r = sqGen.next(size);
		return new Long[] {r.getFrom(), r.getTo()};
	}

	@Override
	public boolean adjust(Long arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
