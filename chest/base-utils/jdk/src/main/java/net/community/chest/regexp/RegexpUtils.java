/*
 *
 */
package net.community.chest.regexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 14, 2011 8:55:22 AM
 */
public final class RegexpUtils {
    private RegexpUtils ()
    {
        // no instance
    }
    /**
     * @param s Initial match pattern {@link String}
     * @return Adjusted pattern (may be same as input if no replacements) where:</P></BR>
     * <UL>
     *         <LI><code>*</code> is replaced by <code>.*</code></LI>
     *         <LI><code>.</code> is replaced by <code>\\.</code></LI>
     * </UL>
     * <B>Note:</B> these replacements occur only if not already such in the original string
     */
    public static final String adjustScanPattern (final String s)
    {
        final String    p=StringUtil.getCleanStringValue(s);
        final int        pLen=(null == p) ? 0 : p.length();
        if (pLen <= 0)
            return p;

        StringBuilder    sb=null;
        int                lastPos=0;
        for (int    curPos=0; curPos < pLen; curPos++)
        {
            final char    c=p.charAt(curPos);
            String        repVal=null;
            if ('*' == c)
            {
                if ((curPos > 0) && (p.charAt(curPos-1) == '.'))
                    continue;

                repVal = ".*";
            }
            else if ('.' == c)
            {
                if ((curPos < (pLen-1)) && (p.charAt(curPos+1) == '*'))
                    continue;

                repVal = "\\.";
            }

            if ((null == repVal) || (repVal.length() <= 0))
                continue;

            if (null == sb)
                sb = new StringBuilder(pLen + 4);

            final String    clrText=(curPos > lastPos) ? p.substring(lastPos, curPos) : null;
            if ((clrText != null) && (clrText.length() > 0))
                sb.append(clrText);
            sb.append(repVal);

            lastPos = curPos + 1;
        }

        if ((null == sb) || (sb.length() <= 0))
            return p;

        // check if any leftovers
        if (lastPos < pLen)
        {
            final String    remText=p.substring(lastPos);
            sb.append(remText);
        }

        return sb.toString();
    }

    public static final List<Pattern> getPatternsList (final String s, final char sepChar)
        throws PatternSyntaxException
    {
        final Collection<String>    sl=StringUtil.splitString(s, sepChar);
        final int                    numPatterns=(null == sl) ? 0 : sl.size();
        if (numPatterns <= 0)
            return null;

        List<Pattern>    ret=null;
        for (final String ps : sl)
        {
            final String    scanPattern=adjustScanPattern(ps);
               final Pattern    p=
                   ((null == scanPattern) || (scanPattern.length() <= 0)) ? null : Pattern.compile(scanPattern);
               if (null == p)
                   continue;
               if (null == ret)
                   ret = new ArrayList<Pattern>(numPatterns);
               ret.add(p);
           }

        return ret;
    }
    /**
     * @param inputString Input {@link String} to be checked
     * @param p The {@link Pattern} to use for matching
     * @return Whether the pattern matches the string - <code>null</code> if
     * <code>null</code>/empty input or pattern
     */
    public static final Boolean applyPattern (final String inputString, final Pattern p)
    {
        final Matcher    m=
            ((null == p) || (null == inputString) || (inputString.length() <= 0)) ? null : p.matcher(inputString);
        if (null == m)
            return null;

        return Boolean.valueOf(m.matches());
    }
    /**
     * @param inputString Input {@link String} to be checked
     * @param pl A {@link Collection} of {@link Pattern}s to be checked
     * @return The <U>first</U> matching pattern - <code>null</code> if no
     * match (or no input or no patterns)
     * @see #applyPattern(String, Pattern)
     */
    public static final Pattern findMatchingPattern (
            final String inputString, final Collection<? extends Pattern>    pl)
    {
        if ((null == inputString) || (inputString.length() <= 0)
                 || (null == pl) || (pl.size() <= 0))
                     return null;

        for (final Pattern    p : pl)
        {
            final Boolean    res=applyPattern(inputString, p);
            if (res == null)
                continue;
            if (res.booleanValue())
                return p;
        }

        return null;
    }
    /**
     * @param inputString Input {@link String} to be checked
     * @param pl An array of {@link Pattern}s to be checked
     * @return The <U>first</U> matching pattern - <code>null</code> if no
     * match (or no input or no patterns)
     */
    public static final Pattern findMatchingPattern (final String inputString, final Pattern ... pl)
    {
        return findMatchingPattern(inputString, ((pl == null) || (pl.length <= 0)) ? null : Arrays.asList(pl));
    }
    /**
     * @param inputString Input {@link String} to be checked
     * @param pl A {@link Collection} of {@link Pattern}s to be checked
     * @return Whether any pattern matches the input string - <code>null</code> if
     * <code>null</code>/empty input or patterns
     * @see #findMatchingPattern(String, Collection)
     */
    public static final Boolean checkPatterns (
            final String inputString, final Collection<? extends Pattern>    pl)
    {
        if ((null == inputString) || (inputString.length() <= 0)
         || (null == pl) || (pl.size() <= 0))
             return null;

        final Pattern    p=findMatchingPattern(inputString, pl);
        return Boolean.valueOf(p != null);
    }
    /**
     * @param inputString Input {@link String} to be checked
     * @param pl An array of {@link Pattern}s to be checked
     * @return Whether any pattern matches the input string - <code>null</code> if
     * <code>null</code>/empty input or patterns
     */
    public static final Boolean checkPatterns (final String inputString, final Pattern ...    pl)
    {
        return checkPatterns(inputString, ((pl == null) || (pl.length <= 0)) ? null : Arrays.asList(pl));
    }
}
