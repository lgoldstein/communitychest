/*
 *
 */
package net.community.chest.jfree.jfreechart.data.time;

import org.jfree.data.time.RegularTimePeriod;

import net.community.chest.math.ExtraMath;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 21, 2011 9:21:39 AM
 *
 */
public class RegularTimePeriodComparator extends AbstractComparator<RegularTimePeriod> {
    private static final long serialVersionUID = -6300161834289507962L;

    public RegularTimePeriodComparator (boolean ascending)
    {
        super(RegularTimePeriod.class, !ascending);
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (RegularTimePeriod v1, RegularTimePeriod v2)
    {
        if (v1 == v2)
            return 0;
        // push null(s) to end
        if (v1 == null)
            return (+1);
        if (v2 == null)
            return (-1);

        int    nRes=ExtraMath.sign(v1.getFirstMillisecond() - v2.getFirstMillisecond());
        if (nRes != 0)
            return nRes;
        if ((nRes=ExtraMath.sign(v1.getLastMillisecond() - v2.getLastMillisecond())) != 0)
            return nRes;
        return 0;
    }

    public static final RegularTimePeriodComparator ASCENDING=new RegularTimePeriodComparator(true),
                                                    DESCENDING=new RegularTimePeriodComparator(false);
}
