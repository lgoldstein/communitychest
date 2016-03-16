/**
 *
 */
package net.community.chest.util.compare;

import java.util.ArrayList;
import java.util.List;

import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.NumberTables;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Compares 2 version {@link String}-s. Versions are assumed to contain
 * dot separated <U>numbers</U> except for the <U>last</U> component that may
 * contain letter and/or be separated with a <U>hyphen</U> from the rest</P>
 *
 * @author Lyor G.
 * @since Aug 17, 2008 8:53:47 AM
 */
public class VersionComparator extends AbstractComparator<String> {
    /**
     *
     */
    private static final long serialVersionUID = 9075543005987679429L;

    public VersionComparator (boolean ascending)
    {
        super(String.class, !ascending);
    }

    public static final List<String> getSubVersionComponents (final String c)
    {
        final int    cLen=(null == c) ? 0 : c.length();
        if (cLen <= 0)
            return null;

        final Boolean    t=NumberTables.checkNumericalValue(c);
        // if simple number then do nothing
        if ((null == t) || (!t.booleanValue()))
        {
            final int    hPos=c.indexOf('-');
            // check if component format is "a-b"
            if ((hPos > 0) && (hPos < (cLen - 1)))
            {
                final String        p=c.substring(0, hPos),
                                    s=c.substring(hPos + 1);
                final List<String>    pl=getSubVersionComponents(p),
                                    sl=getSubVersionComponents(s);
                if ((null == pl) || (pl.size() <= 0))
                    return sl;    // should not happen
                if ((sl != null) && (sl.size() > 0))    // should not be otherwise
                    pl.addAll(sl);

                return pl;
            }
            else    // check if component format is "3a"
            {
                for (int    lPos=0; lPos < cLen; lPos++)
                {
                    final char    ch=c.charAt(lPos);
                    if ((ch >= '0') && (ch <= '9'))
                        continue;

                    // if no digits to start with then go no further
                    if (lPos <= 0)
                        break;

                    final String        n=c.substring(0, lPos),
                                        s=c.substring(lPos);
                    final List<String>    l=new ArrayList<String>();
                    l.add(n);
                    l.add(s);
                    return l;
                }
            }
        }

        // DON'T USE Arrays.asList() SINCE WE MAY WANT TO ADD TO THIS LIST !!!
        final List<String>    l=new ArrayList<String>();
        l.add(c);
        return l;
    }

    public static final List<String> getVersionComponents (final String v)
    {
        final List<String>    vl=StringUtil.splitString(v, '.');
        final int            numComps=(null == vl) ? 0 : vl.size();
        if (numComps <= 0)
            return vl;

        // TODO go over all the components and check if need to further sub-split them

        // special handling for last component
        final String        lc=vl.get(numComps - 1);
        final List<String>    sl=getSubVersionComponents(lc);
        final int            slSize=(null == sl) ? 0 : sl.size();
        if (slSize <= 1)
            return vl;    // if no further sub-components then do nothing

        // need to re-build the List since "splitString" returns an un-modifiable list
        final List<String>    nl=new ArrayList<String>(numComps + slSize);
        nl.addAll(vl);
        nl.remove(numComps - 1);
        nl.addAll(sl);
        return nl;
    }

    public static final int compareVersionComponents (final String c1, final String c2)
    {
        final Boolean    t1=NumberTables.checkNumericalValue(c1),
                        t2=NumberTables.checkNumericalValue(c2);
        // prefer numbers first
        if (null == t1)    // c1 not a number
            return (null == t2) ? StringUtil.compareDataStrings(c1, c2, true) : (+1);
        if (null == t2)    // c2 not a number
            return (-1);

        // if both integer(s)
        if (t1.booleanValue() && t2.booleanValue())
        {
            // NOTE: we assume no more than Integer(s)
            final int    v1=Integer.parseInt(c1), v2=Integer.parseInt(c2);
            return (v1 - v2);
        }
        else
        {
            final float    v1=Float.parseFloat(c1), v2=Float.parseFloat(c2);
            if (v1 < v2)
                return (-1);
            else if (v1 > v2)
                return (+1);
            else
                return 0;
        }
    }

    public static final int    compareVersions (final String v1, final String v2)
    {
        final List<String>    cl1=getVersionComponents(v1),
                            cl2=getVersionComponents(v2);
        final int            n1=(null == cl1) ? 0 : cl1.size(),
                            n2=(null == cl2) ? 0 : cl2.size(),
                            ml=Math.min(n1,n2),
                            cl=Math.max(ml,0);
        // check components up to common length
        for (int    cIndex=0; cIndex < cl; cIndex++)
        {
            final String    c1=cl1.get(cIndex), c2=cl2.get(cIndex);
            final int        nRes=compareVersionComponents(c1, c2);
            if (nRes != 0)
                return nRes;
        }

        // if same prefix, then shorter ID comes first
        return (n1 - n2);
    }

    public static final char    EXACT_VERSION_COMP_MODIFIER='*',
                                ATLEAST_VERSION_COMP_MODIFIER='+',
                                ATMOST_VERSION_COMP_MODIFIER='-';

    public static final Integer getAdjustedComparisonResult (final int nRes, final char modChar)
    {
        switch(modChar)
        {
            case EXACT_VERSION_COMP_MODIFIER    :
                break;

            case ATLEAST_VERSION_COMP_MODIFIER    :
                if (nRes < 0)    // can stop right here with a "compatible" result
                    return Integer.valueOf(0);
                else if (nRes > 0)
                    return Integer.valueOf(-1);
                break;

            case ATMOST_VERSION_COMP_MODIFIER    :
                if (nRes > 0)    // can stop right here with a "compatible" result
                    return Integer.valueOf(0);
                else if (nRes < 0)
                    return Integer.valueOf(+1);
                break;

            default                                : // do nothing
        }

        if (nRes != 0)
            return Integer.valueOf(nRes);
        return null;
    }
    /**
     * Checks if a version {@link String} matches a "compatibility" expression.
     * @param ver The version to be checked - if null/empty then automatically
     * incompatible. The version is assumed to contain dot-separated
     * <U>numbers</U> (at least in the part being checked).
     * @param req The required "compatibility" expression - assumed to contain
     * only dot-separated <U>numbers</U>, except for the <U>last</U> component
     * which <I>may</I> contain an additional "match modifier" character <U>in
     * addition</U> to a number:</BR>
     * <UL>
     *         <LI>
     *         + - version component value must be <U>at least</U> as specified.
     *         E.g., "1.6+" means that matched version must have at least a "6"
     *         as its second component.
     *         </LI>
     *
     *         <LI>
     *         - - version component value must be <U>at most</U> as specified.
     *         E.g., "1.6-" means that matched version must have at most a "6"
     *         as its second component.
     *         </LI>
     *
     *         <LI>
     *         * - version component value must be <U>exactly</U> as specified.
     *         E.g., "1.6*" means that matched version must have only a "6"
     *         as its second component.
     *         </LI>
     * </UL>
     * <B>Note(s):</B></BR>
     * <UL>
     *         <LI>
     *         once a modifier has been encountered and evaluated the* rest of
     *         the version components are not compared (e.g., "1.6*" matches
     *         "1.6.0.1", "1.6", "1.6.2").
     *         </LI>
     *
     *         <LI>
     *         if no modifier provided, then an <U>exact</U> match is indicated
     *         </LI>
     * </UL>
     * @return An <code>int</code> value indicating the match - where:
     * <UL>
     *         <LI>
     *         zero - version is compatible with the requested pattern
     *         </LI>
     *
     *         <LI>
     *         negative - version is below requested pattern (applicable only
     *         to '+' and '*' modifiers)
     *         </LI>
     *
     *         <LI>
     *         positive - version is above requested pattern (applicable only
     *         to '-' and '*' modifiers)
     *         </LI>
     * </UL>
     * <B>Note(s):</B></BR>
     * <UL>
     *         <LI>
     *         if null/empty compatibility expression provided then a
     *         <U>positive</U> "no match" value is returned
     *         </LI>
     *
     *         <LI>
     *         if null/empty version expression provided then a <U>negative</U>
     *         "no match" value is returned
     *         </LI>
     *
     *         <LI>
     *         if <U>both</U> arguments are null/empty then a <U>"match"<U>
     *         (zero) value is returned
     *         </LI>
     * </UL>
     * @throws NumberFormatException if bad/illegal version/match expression
     * provided (e.g., too many
     */
    public static final int compareVersionCompatibility (final String ver, final String req) throws NumberFormatException
    {
        if ((null == ver) || (ver.length() <= 0))
        {
            if ((null == req) || (req.length() <= 0))
                return 0;
            else
                return (-1);
        }
        else
        {
            if ((null == req) || (req.length() <= 0))
                return (+1);
        }

        final List<String>    vl=getVersionComponents(ver),
                            rl=getVersionComponents(req);
        final int            numVComps=(null == vl) ? 0 : vl.size();
        int                    numRComps=(null == rl) ? 0 : rl.size();
        // check if have a modifier character on the last expression component
        final char            modChar;
        {
            final String    lastComp=(rl == null) ? null : rl.get(numRComps - 1);
            final int        lcLen=(null == lastComp) ? 0 : lastComp.length();
            modChar = (lcLen == 1) ? lastComp.charAt(0) : '\0';

            // if found a modifier character then remove it
            if ((ATLEAST_VERSION_COMP_MODIFIER == modChar)
             || (ATMOST_VERSION_COMP_MODIFIER == modChar)
             || (EXACT_VERSION_COMP_MODIFIER == modChar))
            {
                numRComps--;
                rl.remove(numRComps);
            }
        }

        final int    cComps=Math.min(numVComps, numRComps);
        if (cComps <= 0)    // should not happen (since we already checked BOTH for null/empty)
            throw new NumberFormatException("compareVersionCompatibility(" + ver + ")[" + req + "] failed to extract components");

        for (int    cIndex=0; cIndex < cComps; cIndex++)
        {
            final String    vc=vl.get(cIndex),
                            rc=rl.get(cIndex);
            final int        vNum=Integer.parseInt(vc),
                            rNum=Integer.parseInt(rc);
            final Integer    cDiff=getAdjustedComparisonResult(rNum - vNum, modChar);
            if (cDiff != null)
                return cDiff.intValue();
        }

        // if more expression components than in the version
        if (numRComps > numVComps)
            return (-1);    // the version is "below" expected

        // we do not check beyond the expression's number of components
        return 0;
    }
    /*
     * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (final String v1, final String v2)
    {
        return compareVersions(v1, v2);
    }

    public static final VersionComparator    ASCENDING=new VersionComparator(true),
                                            DESCENDING=new VersionComparator(false);
}
