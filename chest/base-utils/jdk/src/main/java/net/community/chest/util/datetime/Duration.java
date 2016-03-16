/*
 *
 */
package net.community.chest.util.datetime;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.math.LongsComparator;
import net.community.chest.lang.math.NumberTables;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 16, 2008 3:03:25 PM
 */
public final class Duration extends Number implements Cloneable, Comparable<Duration> {
    /**
     *
     */
    private static final long serialVersionUID = 899885869117242840L;
    public static final long fromTimespec (final Collection<? extends Map.Entry<TimeUnits,? extends Number>>    tml)
    {
        if ((null == tml) || (tml.size() <= 0))
            return 0L;

        long    timeVal=0L;
        for (final Map.Entry<TimeUnits,? extends Number> te : tml)
        {
            final TimeUnits    mulFactor=(null == te) ? null : te.getKey();
            final Number    mulValue=(null == te) ? null : te.getValue();
            if ((null == mulFactor) || (null == mulValue))
                throw new NumberFormatException("fromTimespec() bad spec result");

            final long    addValue=mulFactor.getMilisecondValue(mulValue.longValue());
            timeVal += addValue;
        }

        return timeVal;
    }

    public static final long fromTimespec (final Map<TimeUnits,? extends Number> spec)
    {
        return fromTimespec(((null == spec) || (spec.size() <= 0)) ? null : spec.entrySet());
    }
    /**
     * @param v a time specification value - e.g. 1H2m, 1d
     * @return time specification value in msec. - may be zero if null/empty
     * specification provided
     * @throws NumberFormatException if invalid format
     */
    public static final long fromTimespec (final CharSequence v) throws NumberFormatException
    {
        final Map<TimeUnits,Long>    tm=TimeUnits.parseTimespec(v);
        final long                    timeVal=fromTimespec(tm);
        return timeVal;
    }

    public static final Map<TimeUnits,Long> fromTimespec (final long timeVal) throws NumberFormatException
    {
        if (timeVal < 0L)
            throw new NumberFormatException("fromTimespec(" + timeVal + ") negative values N/A");

        final Map<TimeUnits,Long>    spec=new EnumMap<TimeUnits,Long>(TimeUnits.class);
        // shortcut (covers ZERO as well)
        if (timeVal < DateUtil.MSEC_PER_SECOND)
        {
            spec.put(TimeUnits.MILLISECOND, Long.valueOf(timeVal));
            return spec;
        }

        // we go "downwards" in the durations
        final Collection<TimeUnits>    va=TimeUnits.getValues(Boolean.FALSE);
        long                        remTime=timeVal;
        for (final TimeUnits u : va)
        {
            final long    sz=(null == u) ? 0L : u.getMilisecondValue();
            if ((sz <= 0L)            // should not happen
             || (sz > remTime))        // means have less than 1 unit
                continue;

            final long    mulFactor=remTime / sz;
            spec.put(u, Long.valueOf(mulFactor));

            if (0 == (remTime %= sz))
                break;
        }

        if (remTime > 0L)    // should not happen
            throw new NumberFormatException("fromTimespec(" + timeVal + ") incomplete conversion (remaining=" + remTime + ")");

        return spec;
    }

    public static final Map<TimeUnits,Long> fromTimespec (final Number n) throws NumberFormatException
    {
        return (null == n) ? null : fromTimespec(n.longValue());
    }

    public static final String toString (final Map<TimeUnits,? extends Number> sm)
    {
        final int    ss=(null == sm) ? 0 : sm.size();
        if (ss <= 0)
            return null;

        // we go "downwards" in the durations
        final StringBuilder            sb=new StringBuilder(ss * NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM);
        final Collection<TimeUnits>    va=TimeUnits.getValues(Boolean.FALSE);
        for (final TimeUnits u : va)
        {
            final char        c=(null == u) ? '\0' : u.getFormatChar();
            final Number    l=(null == u) ? null : sm.get(u);
            if ((null == l) || ('\0' == c))
                continue;

            sb.append(l).append(c);
        }

        return sb.toString();
    }

    public static final String toString (final long duration) throws NumberFormatException
    {
        final Map<TimeUnits,? extends Number> sm=fromTimespec(duration);
        return toString(sm);
    }

    public static final String toString (final Number n) throws NumberFormatException
    {
        return (null == n) ? null : toString(n.longValue());
    }

    private final Map<TimeUnits,Long>    _spec;
    public Map<TimeUnits,Long> getSpecification ()
    {
        return _spec;
    }

    private final long    _timeVal;
    /*
     * @see java.lang.Number#longValue()
     */
    @Override
    public long longValue ()
    {
        return _timeVal;
    }

    private final String    _strVal;
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return _strVal;
    }

    public Duration (final Map<TimeUnits,Long> spec)
    {
        _spec = spec;
        _timeVal = fromTimespec(spec);
        _strVal = toString(spec);
    }

    public Duration (final CharSequence v) throws NumberFormatException, IllegalStateException
    {
        if ((null == v) || (v.length() <= 0))
            throw new NumberFormatException("No data provided");

        _spec = TimeUnits.parseTimespec(v);
        _timeVal = fromTimespec(_spec);
        _strVal = v.toString();
    }

    public Duration (final long v) throws NumberFormatException, IllegalStateException
    {
        if ((_timeVal=v) < 0L)
            throw new NumberFormatException("<init>(" + v + ") negative N/A");

        _spec = fromTimespec(v);
        _strVal = toString(_spec);
    }
    /*
     * @see java.lang.Number#doubleValue()
     */
    @Override
    public double doubleValue ()
    {
        return longValue();
    }
    /*
     * @see java.lang.Number#floatValue()
     */
    @Override
    public float floatValue ()
    {
        return longValue();
    }
    /*
     * @see java.lang.Number#intValue()
     */
    @Override
    public int intValue ()
    {
        final long    v=longValue();
        return (int) (v & 0x0FFFFFFFFL);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public Duration clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo (final Duration o)
    {
        if (null == o)
            return (-1);

        if (this == o)
            return 0;

        final long    tl=longValue(), ol=o.longValue();
        return LongsComparator.compare(tl, ol);
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (!(obj instanceof Duration))
            return false;
        if (this == obj)
            return true;

        return (((Duration) obj).longValue() == longValue());
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return intValue();
    }
}
