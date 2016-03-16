package net.community.chest.jmx;

import java.util.Comparator;
import java.util.Map;

import javax.management.MBeanAttributeInfo;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P{@link Comparator} used to order returned attributes values in
 * lexicographical order according to their names</P>
 * @author Lyor G.
 * @since Dec 26, 2007 8:10:29 AM
 */
public class AttrInfoComparator implements Comparator<Map.Entry<MBeanAttributeInfo,Object>> {
    public AttrInfoComparator ()
    {
        super();
    }

    public static final int compare (String n1, String n2)
    {
        if ((null == n1) || (n1.length() <= 0))
            return ((null == n2) || (n2.length() <= 0)) ? 0 : (-1);
        else if ((null == n2) || (n2.length() <= 0))
            return (+1);
        else
            return n1.compareToIgnoreCase(n2);
    }

    public static final int compare (MBeanAttributeInfo i1, MBeanAttributeInfo i2)
    {
        return compare((null == i1) ? null : i1.getName(), (null == i2) ? null : i2.getName());
    }
    /*
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare (Map.Entry<MBeanAttributeInfo,Object> o1, Map.Entry<MBeanAttributeInfo,Object> o2)
    {
        return compare((null == o1) ? null : o1.getKey(), (null == o2) ? null : o2.getKey());
    }

    public static final AttrInfoComparator    DEFAULT=new AttrInfoComparator();
}
