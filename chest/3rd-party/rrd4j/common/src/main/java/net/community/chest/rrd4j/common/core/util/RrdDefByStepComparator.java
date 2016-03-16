package net.community.chest.rrd4j.common.core.util;

import org.rrd4j.core.RrdDef;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> The {@link RrdDef} generic type
 * @author Lyor G.
 * @since Jan 10, 2008 11:14:31 AM
 */
public class RrdDefByStepComparator<D extends RrdDef> extends AbstractRrdDefComparator<D> {
    public RrdDefByStepComparator (Class<D> defClass, boolean ascending) throws IllegalArgumentException
    {
        super(defClass, !ascending);
    }
    /*
     * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (D o1, D o2)
    {
        // push null(s) to end
        if (null == o1)
            return (null == o2) ? 0 : (+1);
        else if (null == o2)
            return (-1);

        final long    s1=o1.getStep(), s2=o2.getStep(), sDiff=s1 - s2;
        if (sDiff < 0L)
            return (-1);
        else if (sDiff > 0L)
            return (+1);

        return 0;
    }

    public static final RrdDefByStepComparator<RrdDef>    ASCENDING=new RrdDefByStepComparator<RrdDef>(RrdDef.class, true),
                                                        DESCENDING=new RrdDefByStepComparator<RrdDef>(RrdDef.class, false);
}
