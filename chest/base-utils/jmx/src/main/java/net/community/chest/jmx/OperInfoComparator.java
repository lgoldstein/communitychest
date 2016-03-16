package net.community.chest.jmx;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>A {@link java.util.Comparator} used to sort {@link MBeanOperationInfo} entries
 * according to lexicographical order of their operation name(s)
 * @author Lyor G.
 * @since Dec 26, 2007 8:12:22 AM
 */
public class OperInfoComparator extends AbstractComparator<MBeanOperationInfo> {
    /**
     *
     */
    private static final long serialVersionUID = 6072914138866568626L;
    public OperInfoComparator (boolean ascending)
    {
        super(MBeanOperationInfo.class, !ascending);
    }

    public static final int compareOperations (MBeanOperationInfo o1, MBeanOperationInfo o2)
    {
        final int    nRes=StringUtil.compareDataStrings((null == o1) ? null : o1.getName(), (null == o2) ? null : o2.getName(), false);
        if (nRes != 0)
            return nRes;

        // if same name, then use number of parameters
        final MBeanParameterInfo[]    p1=(null == o1) ? null : o1.getSignature(),
                                    p2=(null == o2) ? null : o2.getSignature();

        final int    numP1=(null == p1) ? 0 : p1.length,
                    numP2=(null == p2) ? 0 : p2.length;
        return (numP2 - numP1);
    }
    /*
     * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (MBeanOperationInfo o1, MBeanOperationInfo o2)
    {
        return compareOperations(o1, o1);
    }
    // MBeanOperationInfo entries comparator instance(s)
    public static final OperInfoComparator    ASCENDING=new OperInfoComparator(true),
                                            DESCENDING=new OperInfoComparator(false);
}
