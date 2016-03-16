/*
 *
 */
package net.community.chest.regexp;

import java.util.regex.Pattern;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 14, 2011 9:20:35 AM
 */
public class PatternComparator extends AbstractComparator<Pattern> {
    /**
     *
     */
    private static final long serialVersionUID = 8142223814330028243L;

    public PatternComparator (boolean ascending)
    {
        super(Pattern.class, !ascending);
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (Pattern v1, Pattern v2)
    {
        final String    p1=(v1 == null) ? null : v1.pattern(),
                        p2=(v2 == null) ? null : v2.pattern();
        return StringUtil.compareDataStrings(p1, p2, true /* patterns are always case-sensitive */);
    }

    public static final PatternComparator    ASCENDING=new PatternComparator(true),
                                            DESCENDING=new PatternComparator(false);
}
