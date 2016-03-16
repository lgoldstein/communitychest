/*
 *
 */
package net.community.chest.jmx;

import javax.management.ObjectName;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 15, 2011 9:05:43 AM
 */
public class CanonicalObjectNameComparator extends AbstractObjectNameComparator {
    /**
     *
     */
    private static final long serialVersionUID = 3905742363914881483L;

    public CanonicalObjectNameComparator (boolean ascending)
    {
        super(ascending);
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (ObjectName v1, ObjectName v2)
    {
        return StringUtil.compareDataStrings((v1 == null) ? null : v1.getCanonicalName(), (v2 == null) ? null : v2.getCanonicalName(), false);
    }

    public static final CanonicalObjectNameComparator    ASCENDING=new CanonicalObjectNameComparator(true),
                                                        DESCENDING=new CanonicalObjectNameComparator(false);
}
