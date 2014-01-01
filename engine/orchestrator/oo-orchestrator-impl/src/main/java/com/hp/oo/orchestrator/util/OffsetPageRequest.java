package com.hp.oo.orchestrator.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * User: wahnonm
 * Date: 17/04/13
 * Time: 19:07
 */
public class OffsetPageRequest implements Pageable {

    private final int size;
    private final Sort sort;
    private final int offset;

    public OffsetPageRequest(int offset, int pageSize, Sort sort){
        this.size = pageSize;
        this.sort = sort;
        this.offset = offset;
    }

    @Override
    public int getPageNumber() {
        return 0;
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

	@Override
	public Pageable next() {
		return null;
	}

	@Override
	public Pageable previousOrFirst() {
		return null;
	}

	@Override
	public Pageable first() {
		return null;
	}

	@Override
	public boolean hasPrevious() {
		return false;
	}
}
