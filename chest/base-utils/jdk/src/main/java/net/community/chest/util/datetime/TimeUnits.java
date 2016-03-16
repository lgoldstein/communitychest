package net.community.chest.util.datetime;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.map.BooleansMap;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Nov 27, 2007 1:31:41 PM
 */
public enum TimeUnits implements CalendarFieldSetter {
    MILLISECOND(1L, 'S', Calendar.MILLISECOND),
    SECOND(1000L, 's', Calendar.SECOND),
    MINUTE(SECOND.getMilisecondValue() * 60L, 'm', Calendar.MINUTE),
    HOUR(MINUTE.getMilisecondValue() * 60L, 'H', Calendar.HOUR_OF_DAY),
    DAY (HOUR.getMilisecondValue() * 24L, 'd', Calendar.DAY_OF_MONTH),
    WEEK(DAY.getMilisecondValue() * 7L, 'w', Calendar.WEEK_OF_YEAR),
    MONTH(DAY.getMilisecondValue() * 30L, 'M', Calendar.MONTH),
    YEAR(DAY.getMilisecondValue() * 365L, 'y', Calendar.YEAR);

    private final long _milis;
    public long getMilisecondValue ()
    {
        return _milis;
    }

    private final char    _fmtChar;
    public char getFormatChar ()
    {
        return _fmtChar;
    }

    private final int    _calField;
    /*
     * @see net.community.chest.util.datetime.CalendarFieldIndicator#getCalendarValue()
     */
    @Override
    public int getCalendarFieldId ()
    {
        return _calField;
    }
    /*
     * @see net.community.chest.util.datetime.CalendarFieldIndicator#getFieldValue(java.util.Calendar)
     */
    @Override
    public int getFieldValue (Calendar c)
    {
        return (c == null) ? (-1) : c.get(getCalendarFieldId());
    }
    @Override
    public void setFieldValue (Calendar c, int value)
    {
        if (c == null)
            return;    // debug breakpoint

        c.set(getCalendarFieldId(), value);
    }
    /**
     * Converts from another unit into this one
     * @param numUnits Number of {@link TimeUnits} of the &quot;other&quot;
     * unit
     * @param unit The &quot;other&quot; unit
     * @return The number of units required to represent the &quot;other&quot;
     * unit into &quot;this&quot; one
     * @throws IllegalArgumentException if no &quot;other&quot; unit instance
     * provided
     */
    public double convertToThisUnit (final double numUnits, final TimeUnits unit) throws IllegalArgumentException
    {
        if (null == unit)
            throw new IllegalArgumentException("convertToThisUnit(" + numUnits + ") no other unit specified");

        final long    thisFactor=getMilisecondValue(), otherFactor=unit.getMilisecondValue();
        if (thisFactor == otherFactor)
            return numUnits;

        final double    unitFactor=(double) thisFactor / (double) otherFactor;
        return unitFactor * numUnits;
    }

    TimeUnits (long milis, char fmtChar, int calField)
    {
        _milis = milis;
        _fmtChar = fmtChar;
        _calField = calField;
    }
    /**
     * @param numUnits Number of units to serve as multiplier
     * @return Number of msec. representing the requested number of units
     */
    public long getMilisecondValue (long numUnits)
    {
        return numUnits * getMilisecondValue();
    }
    /**
     * @param msecValue Milliseconds value
     * @return <U>Closest</U> number of units contained in the specified msec.
     */
    public double getNumUnits (long msecValue)
    {
        return (double) msecValue / getMilisecondValue();
    }
    /**
     * @param msecValue Milliseconds value
     * @return A &quot;pair&quot; represented as a {@link java.util.Map.Entry} whose
     * key is the quotient and value is the remainder of dividing the provided
     * value with the units size.
     */
    public Map.Entry<Long,Long> getUnitsValues (long msecValue)
    {
        final long    tv=getMilisecondValue(),
                    q=(msecValue / tv),
                    r=(msecValue % tv);
        return new MapEntryImpl<Long,Long>(Long.valueOf(q), Long.valueOf(r));
    }
    @SuppressWarnings({ "cast", "unchecked", "rawtypes" })
    private static final BooleansMap<List<TimeUnits>>    _unitsMap=
        (BooleansMap<List<TimeUnits>>) new BooleansMap(List.class, true /* allow null key */);
    /**
     * Returns a (cached) array of {@link TimeUnits} sorted by duration
     * according to the provided parameter
     * @param ascending Sort direction (<code>null</code> means un-sorted)
     * @return Array of {@link TimeUnits} sorted by duration (if
     * non-<code>null</code> sort direction specified)
     */
    public static final List<TimeUnits> getValues (final Boolean ascending /* null == unsorted */)
    {
        synchronized(_unitsMap)
        {
            List<TimeUnits>    vl=_unitsMap.get(ascending);
            if (null == vl)
            {
                final TimeUnits[]    va=values();

                if (ascending != null)
                {
                    final Comparator<TimeUnits>    c=ascending.booleanValue()
                        ? ByDurationTimeUnitsComparator.ASCENDING
                        : ByDurationTimeUnitsComparator.DESCENDING
                        ;
                    Arrays.sort(va, c);
                }

                vl = Collections.unmodifiableList(Arrays.asList(va));
                _unitsMap.put(ascending, vl);
            }

            return vl;
        }
    }

    public static final List<TimeUnits> getValues ()
    {
        return getValues(null);
    }

    public static final TimeUnits fromString (final String s)
    {
        return CollectionsUtils.fromString(getValues(), s, false);
    }

    public static final TimeUnits fromFormatChar (final char c)
    {
        final Collection<TimeUnits>    vals=getValues();
        if ((null == vals) || (vals.size() <= 0))
            return null;    // should not happen

        for (final TimeUnits u : vals)
        {
            if ((u != null) /* should not be otherwise */ && (u.getFormatChar() == c))
                return u;
        }

        return null;
    }
    // NOTE !!! looks only at 1st character
    public static final TimeUnits fromFormatChar (final CharSequence cs)
    {
        if ((null == cs) || (cs.length() <= 0))
            return null;

        return fromFormatChar(cs.charAt(0));
    }
    /**
     * @param tfc one of the time factor characters
     * @return msec. value representing <U>one</U> unit of this time factor
     * @throws NumberFormatException if bad/illegal time factor specified
     * @see TimeUnits#getFormatChar() for available formatting characters
     */
    public static final long parseTimeFactor (final char tfc) throws NumberFormatException
    {
        final TimeUnits    u=TimeUnits.fromFormatChar(tfc);
        if (null == u)
            throw new NumberFormatException("parseTimeFactor(" + String.valueOf(tfc) + "): unknown factor");

        return u.getMilisecondValue();
    }
    /**
     * @param v a time specification value - e.g. 1H2m, 1d
     * @return A {@link Map} of all the values specified - key=the
     * {@link TimeUnits}, value=the {@link Long} value. May be null/empty
     * if no specification provided
     * @throws NumberFormatException If bad number specified
     * @throws IllegalStateException If same {@link TimeUnits} repeated
     */
    public static final Map<TimeUnits,Long> parseTimespec (final CharSequence v)
        throws NumberFormatException, IllegalStateException
    {
        final int            vLen=(null == v) ? 0 : v.length();
        Map<TimeUnits,Long>    ret=null;
        for (int    curPos=0; curPos < vLen; curPos++)
        {
            int    nextPos=curPos;
            for ( ; nextPos < vLen; nextPos++)
            {
                final char    vch=v.charAt(nextPos);
                if ((vch < '0') || (vch > '9'))
                    break;
            }

            if ((nextPos <= curPos) || (nextPos >= vLen))
                throw new NumberFormatException("parseTimespec(" + v + ") missing value measurement unit specifier");

            final String    subValue=v.subSequence(curPos, nextPos).toString();
            final Long        mulValue=Long.valueOf(subValue);
            final char        tfc=v.charAt(nextPos);
            final TimeUnits    mulFactor=TimeUnits.fromFormatChar(tfc);
            if (null == mulFactor)
                throw new NumberFormatException("parseTimespec(" + v + ") unknown factor: " + String.valueOf(tfc));

            if (null == ret)
                ret = new EnumMap<TimeUnits,Long>(TimeUnits.class);
            else if (ret.containsKey(mulFactor))
                throw new IllegalStateException("parseTimespec(" + v + ") respecified factor: " + mulFactor);
            ret.put(mulFactor, mulValue);

            curPos = nextPos + 1;    // skip modifier char
        }

        return ret;
    }
}
