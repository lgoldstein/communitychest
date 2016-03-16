package net.community.chest.lang.math;


/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 10, 2007 4:16:21 PM
 */
public class IntegersComparator extends DefaultNumbersComparator<Integer> {
    /**
     *
     */
    private static final long serialVersionUID = -3649685841634789093L;
    public IntegersComparator (boolean ascending)
    {
        super(Integer.class, ascending);
    }

    public static final int compare (final int i1, final int i2)
    {
        return (i2 - i1);
    }
    /**
     * Pre-instantiated instances of the comparator
     */
    public static final IntegersComparator    ASCENDING=new IntegersComparator(true),
                                            DESCENDING=new IntegersComparator(false);
}
