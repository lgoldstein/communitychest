package com.vmware.spring.workshop.model.user;

import org.apache.commons.lang3.tuple.MutablePair;

/**
 * @author lgoldstein
 */
public class InvestmentData extends MutablePair<Long,Integer> {
    private static final long serialVersionUID = 7954577426801335771L;

    public InvestmentData () {
        this(-1L, -1);
    }

    // special constructor used by JPQL
    public InvestmentData (Long id, Long value) {
        super(id, (value == null) ? null : Integer.valueOf(value.intValue()));

        if (id == null)
            throw new IllegalStateException("No ID");
        if (value == null)
            throw new IllegalStateException("No value");
        if ((value.longValue() < Integer.MIN_VALUE) || (value.longValue() > Integer.MAX_VALUE))
            throw new IllegalStateException("Bad value: " + value);
    }

    public InvestmentData (long id, int value) {
        super(Long.valueOf(id), Integer.valueOf(value));
    }
}
