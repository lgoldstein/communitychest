package net.community.chest.apache.log4j.helpers;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to hold an "entry" for a layout pattern</P>
 *
 * @author Lyor G.
 * @since Oct 10, 2007 11:21:21 AM
 */
public class LayoutPatternEntry implements Serializable, Cloneable {
    /**
     *
     */
    private static final long serialVersionUID = -3075751708200972955L;
    /**
     * Default (empty) constructor
     */
    public LayoutPatternEntry ()
    {
        super();
    }
    /**
     * Pattern value - null/empty if not initialized
     */
    private String    _name;
    public String getName ()
    {
        return _name;
    }

    public void setName (String patternName)
    {
        _name = patternName;
    }
    /**
     * Pattern modifier (may be null/empty)
     */
    private String    _modifier;
    public String getModifier ()
    {
        return _modifier;
    }

    public void setModifier (String modifier)
    {
        _modifier = modifier;
    }
    /**
     * Fully initialized constructor
     * @param name pattern name (<B>Note:</B> not validated in any way)
     * @param modifier pattern modifier - may be null/empty
     */
    public LayoutPatternEntry (final String name, final String modifier)
    {
        _name = name;
        _modifier = modifier;
    }
    /**
     * Copy constructor
     * @param e entry to copy from - if null then nothing is done
     */
    public LayoutPatternEntry (final LayoutPatternEntry e)
    {
        this((null == e) ? null : e.getName(), (null == e) ? null : e.getModifier());
    }
    /**
     * No-modifier constructor
     * @param name pattern name (<B>Note:</B> not validated in any way)
     */
    public LayoutPatternEntry (final String name)
    {
        this(name, null);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public LayoutPatternEntry clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if ((null == obj) || (!(obj instanceof LayoutPatternEntry)))
            return false;
        if (this == obj)
            return true;

        final LayoutPatternEntry    pe=(LayoutPatternEntry) obj;
        return (0 == StringUtil.compareDataStrings(getName(), pe.getName(), true))
            && (0 == StringUtil.compareDataStrings(getModifier(), pe.getModifier(), true))
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return StringUtil.getDataStringHashCode(getName(), true)
             + StringUtil.getDataStringHashCode(getModifier(), true)
             ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final String    n=getName(),
                        p=String.valueOf(ExtendedPatternParser.PATTERN_PREFIX) + n,
                        m=getModifier();
        if ((null == m) || (m.length() <= 0))
            return p;

        return p + String.valueOf(ExtendedPatternParser.MODSTART_DELIM) + m + String.valueOf(ExtendedPatternParser.MODEND_DELIM);
    }
    /**
     * <P>Parses a sequence of layout patterns. A pattern is defined as a
     * sequence of characters having the following format: <B>%</B><I>pattern</I>[<I>{modifier}</I>].
     * Where the <I>pattern</I> is a <U>single</U> character and the
     * (optional) modifier is a sequence of zero or more characters.
     * <B>Note:</B> usually, if the modifier is null/empty it is customary
     * to drop the enclosing braces as well.</P></BR>
     * <P>The sequence of patterns may contain separators between the patterns
     * in it - e.g., <I>%a%b{foo}</I>, <I>%a,%b{foo}</I>, <I>"%a","%b{foo}"</I>
     * all yield the <U>same</U> parsed result
     * @param cs pattern sequence to be parsed - may be null/empty (if zero
     * length parameter)
     * @param startPos start position in sequence to start parsing (inclusive)
     * @param len number of characters to parse
     * @return {@link Collection} of {@link LayoutPatternEntry}-es representing
     * the parse result. May be null/empty if nothing to parse
     * @throws IllegalArgumentException if bad format/arguments
     */
    public static final Collection<LayoutPatternEntry> parseLayoutPattern (final CharSequence cs, final int startPos, final int len) throws IllegalArgumentException
    {
        final int    maxPos=startPos + len;
        if ((startPos < 0) || (len < 0))
            throw new IllegalArgumentException("parseLayoutPattern(" + cs + ") bad/illegal range: " + startPos + "/" + len);
        if (0 == len)    // OK if nothing to parse
            return null;
        if ((null == cs) || (maxPos > cs.length()))
            throw new IllegalArgumentException("parseLayoutPattern(" + cs + ") bad/illegal sequence: " + startPos + "-" + maxPos);

        Collection<LayoutPatternEntry>    ret=null;
        for (int    curPos=startPos; curPos < maxPos; curPos++)
        {
            // skip till pattern modifier found
            {
                final char    ch=cs.charAt(curPos);
                if (ch != ExtendedPatternParser.PATTERN_PREFIX)
                    continue;    // skip till start of pattern
            }

            // find pattern name
            final String name;
            {
                final int    patPos=curPos + 1;
                if (patPos >= maxPos)
                    throw new IllegalArgumentException("parseLayoutPattern(" + cs + ") missing modifier at position=" + patPos);

                for (curPos=patPos; curPos < maxPos; curPos++)
                {
                    final char    ch=cs.charAt(curPos);
                    if (('-' == ch) || ('+' == ch)
                     || (('0' <= ch) && (ch <= '9'))
                     || (('a' <= ch) && (ch <= 'z'))
                     || (('A' <= ch) && (ch <= 'Z')))
                        continue;    // skip allowed characters in pattern name

                    break;
                }

                final CharSequence    nameSeq=cs.subSequence(patPos, curPos);
                name = (null == nameSeq) ? null : nameSeq.toString();
                if ((null == name) || (name.length() <= 0))
                    throw new IllegalArgumentException("parseLayoutPattern(" + cs + ") missing pattern name at position=" + patPos);
            }

            // extract (optional) modifier
            String    modifier=null;
            // OK if reached end of sequence or non-modifier character
            if ((curPos < maxPos) && (ExtendedPatternParser.MODSTART_DELIM == cs.charAt(curPos)))
            {
                final int    modStart=curPos + 1;
                // find end brace
                for (curPos++ /* skip start brace */; curPos < maxPos; curPos++)
                {
                    if (ExtendedPatternParser.MODEND_DELIM == cs.charAt(curPos))
                    {
                        final int            modLen=curPos - modStart;
                        final CharSequence    modSeq=(modLen > 0) ? cs.subSequence(modStart, curPos) : null /* OK if no modifier */;
                        modifier = ((null == modSeq) || (modSeq.length() <= 0)) ? "" : modSeq.toString();
                        break;
                    }
                }

                if (null == modifier)
                    throw new IllegalArgumentException("parseLayoutPattern(" + cs + ") missing modifier end brace after position=" + modStart);
            }

            final LayoutPatternEntry    pe=new LayoutPatternEntry(name, modifier);
            if (null == ret)
                ret = new LinkedList<LayoutPatternEntry>();
            ret.add(pe);
        }

        return ret;
    }
    /**
     * @param cs pattern sequence to be parsed - may be null/empty
     * @return {@link Collection} of {@link LayoutPatternEntry}-es representing
     * the parse result. May be null/empty if nothing to parse
     * @throws IllegalArgumentException if bad format/arguments
     * @see #parseLayoutPattern(CharSequence, int, int)
     */
    public static final Collection<LayoutPatternEntry> parseLayoutPattern (final CharSequence cs) throws IllegalArgumentException
    {
        return ((null == cs) || (cs.length() <= 0)) ? null : parseLayoutPattern(cs, 0, cs.length());
    }
}
