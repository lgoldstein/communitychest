package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.io.Serializable;
import java.io.UTFDataFormatException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 9:59:31 AM
 */
public class IMAP4FlagValue implements Serializable, PubliclyCloneable<IMAP4FlagValue> {
    /**
     *
     */
    private static final long serialVersionUID = -5801741252689227897L;
    /**
     * Default/empty constructor
     */
    public IMAP4FlagValue ()
    {
        super();
    }
    /**
     * Flag "name"
     */
    private String  _name /* =null */;
    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }
    /**
     * Pre-initialized constructor
     * @param name flag value
     * @throws IllegalArgumentException if null/empty flag value
     */
    public IMAP4FlagValue (String name) throws IllegalArgumentException
    {
        if ((null == (_name=name)) || (name.length() <= 0))
            throw new IllegalArgumentException("Null/empty IMAP4 flag name");
    }
    /**
     * Character preceding system flags
     */
    public static final char IMAP4_SYSFLAG_CHAR='\\';
    /**
     * Character preceding proprietary/user flags
     */
    public static final char IMAP4_USRFLAG_CHAR='$';
    /**
     * @return TRUE if current flag is a system one (FALSE if proprietary)
     * @see #IMAP4_SYSFLAG_CHAR
     * @see #IMAP4_USRFLAG_CHAR
     */
    public boolean isSystemFlag ()
    {
        final String    v=getName();
        if ((null == v) || (v.length() <= 0))
            return false;
        else
            return (IMAP4_SYSFLAG_CHAR == v.charAt(0));
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getName();
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return StringUtil.getDataStringHashCode(getName(), false);
    }

    public boolean isSameFlag (final String s)
    {
        return (0 == StringUtil.compareDataStrings(s, getName(), false));
    }

    public boolean isSameFlag (final IMAP4FlagValue flag)
    {
        if (flag == null)
            return false;
        if (flag == this)
            return true;

        return isSameFlag(flag.getName());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        final Class<?>    oc=(obj == null) ? null : obj.getClass();
        if (oc != getClass())
            return false;

        return isSameFlag((IMAP4FlagValue) obj);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public IMAP4FlagValue clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }

    public static final <A extends Appendable> A appendFlagsList (final A sb, final IMAP4FlagValue[] flags) throws IOException
    {
        if (null == sb)
            throw new IOException(ClassUtil.getExceptionLocation(IMAP4FlagValue.class, "appendFlagsList") + " no " + Appendable.class.getName() + " instance");

        sb.append(IMAP4Protocol.IMAP4_PARLIST_SDELIM);

        for (int    i=0, fdx=0, numFlags=(null == flags) ? 0 : flags.length; i < numFlags; i++)
        {
            final IMAP4FlagValue    f=flags[i];
            if (null == f)  // OK - allow it
                continue;

            final String  val=f.toString();
            if ((null == val) || (val.length() <= 0))   // should not happen
                continue;

            if (fdx > 0)
                sb.append(' ');
            sb.append(val);

            fdx++;
        }

        sb.append(IMAP4Protocol.IMAP4_PARLIST_EDELIM);
        return sb;
    }

    public static final String EMPTY_FLAGS_LIST=String.valueOf(IMAP4Protocol.IMAP4_PARLIST_SDELIM) + IMAP4Protocol.IMAP4_PARLIST_EDELIM;
    /**
     * Builds a string containing the specified flags as a list
     * @param flags flags to be converted into a string (may be null/empty)
     * @return list string value (or null if error)
     */
    public static final String buildFlagsList (final IMAP4FlagValue[] flags)
    {
        final int numFlags=(null == flags) ? 0 : flags.length;
        if (numFlags <= 0)
            return EMPTY_FLAGS_LIST;

        try
        {
            return appendFlagsList(new StringBuilder(numFlags * 16), flags).toString();
        }
        catch(IOException e)    // should not happen
        {
            throw new RuntimeException(e);
        }
    }
    /**
     * Returns a list of flags values that may appear in the parsable string (Note: NIL is allowed)
     * @param ps parsable string to be checked for flags values
     * @param listStart flags data start index - MUST be the '(' location
     * @param listEnd flags data end index - MUST be the ')' location
     * @return flags A {@link Collection} of strings - may be NULL if "()" flags list found
     * @throws UTFDataFormatException if unable to parse correctly
     */
    public static final Collection<String> getFlags (final ParsableString ps, final int listStart, final int listEnd)
        throws UTFDataFormatException
    {
        final int maxIndex=(null == ps) ? 0 : ps.getMaxIndex();
        if ((maxIndex <= 0) || (listStart >= listEnd))
            throw new UTFDataFormatException("No flags list data supplied");

        if ((ps.getCharAt(listStart) != IMAP4Protocol.IMAP4_PARLIST_SDELIM) ||
            (ps.getCharAt(listEnd) != IMAP4Protocol.IMAP4_PARLIST_EDELIM))
            throw new UTFDataFormatException("Bad/Illegal flags list delimiters");

        Collection<String>    flags=null; // will be allocated if necessary
        int                 curPos=(listStart+1);
        while (curPos <= listEnd)
        {
            // check if reached end of list
            if (IMAP4Protocol.IMAP4_PARLIST_EDELIM == ps.getCharAt(curPos))
                break;

            final int flagStart=ps.findNonEmptyDataStart(curPos, listEnd), flagEnd=ps.findNonEmptyDataEnd(flagStart+1, listEnd);
            if ((flagStart < curPos) || (flagEnd <= flagStart) || (flagEnd > maxIndex))
                throw new UTFDataFormatException("No flag value end found");
            curPos = flagEnd;

            final String  flagVal=ps.substring(flagStart, flagEnd);
            if ((null == flagVal) || (flagVal.length() <= 0))
                throw new UTFDataFormatException("Empty flag value");

            if (null == flags)
                flags = new LinkedList<String>();
            flags.add(flagVal);
        }

        if (curPos < listEnd)   // should not happen - we expect to exit EXACTLY at the list end
            throw new UTFDataFormatException("Mis-delimited list data");

        final int    numFlags=(null == flags) /* OK if no flags found */ ? 0 : flags.size();
        if (numFlags <= 0)
            return null;

        return flags;
    }
    /**
     * Returns a list of flags values that may appear in the parsable string (Note: NIL is allowed)
     * @param ps parsable string to be checked for flags values
     * @param startIndex flags data start index
     * @return flags A {@link Collection} of strings - may be NULL if "()" or NIL flags list found
     * @throws UTFDataFormatException if unable to parse correctly
     */
    public static final Collection<String> getFlags (final ParsableString ps, final int startIndex)
            throws UTFDataFormatException
    {
        final int maxIndex=(null == ps) ? 0 : ps.getMaxIndex();
        if (maxIndex <= 0)
            throw new UTFDataFormatException("No flags list data supplied");

        final int listStart=ps.findNonEmptyDataStart(startIndex);
        if ((listStart < startIndex) || (listStart >= maxIndex))
            throw new UTFDataFormatException("No flags list start found");

        // check if this is the NIL keyword
        if (ps.getCharAt(listStart) != IMAP4Protocol.IMAP4_PARLIST_SDELIM)
        {
            final int atomEnd=listStart + IMAP4Protocol.IMAP4_NILChars.length;
            if ((atomEnd > maxIndex) || (!ps.compareTo(listStart, atomEnd, IMAP4Protocol.IMAP4_NILChars, true)))
                throw new UTFDataFormatException("Non-NIL atom in list data");

            // make sure that if more data follows the NIL, then it is either whitespace or a list end delimiter
            if ((atomEnd < maxIndex) && (!ps.isEmptyChar(atomEnd)) && (ps.getCharAt(atomEnd) != IMAP4Protocol.IMAP4_PARLIST_EDELIM))
                throw new UTFDataFormatException("NIL-prefix atom in list data");

            return null;
        }

        // at this point, we know we have a '()' delimited list (maybe null)
        final int listEnd=ps.indexOf(IMAP4Protocol.IMAP4_PARLIST_EDELIM, listStart+1);
        if ((listEnd < listStart) || (listEnd >= maxIndex))
            throw new UTFDataFormatException("Non-delimited list data");

        return getFlags(ps, listStart, listEnd);
    }
    /**
     * Builds a {@link Map} of the supplied flags - key=flag string (case
     * insensitive), value=flag instance
     * @param <T> The mapped {@link IMAP4FlagValue} type
     * @param flags may be null/empty
     * @return requested map - may be null/empty if no flags
     */
    public static final <T extends IMAP4FlagValue> Map<String,T> buildFlagsMap (final Collection<T> flags)
    {
        if ((null == flags) || (flags.size() <= 0))
            return null;

        Map<String,T>    m=null;
        for (final T f: flags)
        {
            final String    fName=(null == f) /* should not happen */ ? null : f.getName();
            if ((null == fName) || (fName.length() <= 0))
                continue;    // should not happen

            if (null == m)
                m = new TreeMap<String, T>(String.CASE_INSENSITIVE_ORDER);
            m.put(fName, f);
        }

        return m;
    }

    public static final boolean compareFlags (final Collection<String> f, final Map<String,? extends IMAP4FlagValue> m)
    {
        if ((null == f) || (f.size() <= 0))
            return ((null == m) || (m.size() <= 0));
        else if ((null == m) || (m.size() <= 0))
            return false;
        else if (f.size() != m.size())
            return false;

        for (final String n : f)
        {
            if ((n != null) && (n.length() > 0) && (null == m.get(n)))
                return false;
        }

        return true;
    }

    public static final boolean compareFlags (final Map<String,? extends IMAP4FlagValue> m1, final Map<String,? extends IMAP4FlagValue> m2)
    {
        if ((null == m1) || (m1.size() <= 0))
            return ((null == m2) || (m2.size() <= 0));
        else if ((null == m2) || (m2.size() <= 0))
            return false;
        else if (m1.size() != m2.size())
            return false;

        return compareFlags(m1.keySet(), m2) && compareFlags(m2.keySet(), m1);
    }

    public static final int calculateFlagsHashCode (final Collection<? extends IMAP4FlagValue> flags)
    {
        int    nRes=0;

        if ((flags != null) && (flags.size() > 0))
        {
            for (final IMAP4FlagValue f : flags)
            {
                if (f != null)    // should not be otherwise
                    nRes += f.hashCode();
            }
        }

        return nRes;
    }
}
