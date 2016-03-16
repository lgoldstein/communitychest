package net.community.chest.util.datetime;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 23, 2008 11:35:37 AM
 */
public enum MonthsValues implements CalendarEnumFieldAccessor {
    JANUARY(Calendar.JANUARY),
    FEBRUARY(Calendar.FEBRUARY),
    MARCH(Calendar.MARCH),
    APRIL(Calendar.APRIL),
    MAY(Calendar.MAY),
    JUNE(Calendar.JUNE),
    JULY(Calendar.JULY),
    AUGUST(Calendar.AUGUST),
    SEPTEMBER(Calendar.SEPTEMBER),
    OCTOBER(Calendar.OCTOBER),
    NOVEMBER(Calendar.NOVEMBER),
    DECEMBER(Calendar.DECEMBER);

    private final int    _fieldValue;
    /*
     * @see net.community.chest.util.datetime.CalendarEnumFieldAccessor#getFieldValue()
     */
    @Override
    public int getFieldValue ()
    {
        return _fieldValue;
    }
    /*
     * @see net.community.chest.util.datetime.CalendarFieldIndicator#getCalendarValue()
     */
    @Override
    public int getCalendarFieldId ()
    {
        return Calendar.MONTH;
    }
    /*
     * @see net.community.chest.util.datetime.CalendarFieldIndicator#getFieldValue(java.util.Calendar)
     */
    @Override
    public int getFieldValue (Calendar c)
    {
        return (c == null) ? (-1) : c.get(getCalendarFieldId());
    }
    /*
     * @see net.community.chest.util.datetime.CalendarEnumFieldAccessor#setFieldValue(java.util.Calendar)
     */
    @Override
    public void setFieldValue (Calendar c)
    {
        if (c != null)
            c.set(getCalendarFieldId(), getFieldValue());
    }

    MonthsValues (int fieldValue)
    {
        _fieldValue = fieldValue;
    }

    public static final List<MonthsValues>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static MonthsValues fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static MonthsValues fromCalendarValue (final int calValue)
    {
        for (final MonthsValues v : VALUES)
        {
            if ((v != null) && (v.getFieldValue() == calValue))
                return v;
        }

        return null;
    }

    public static MonthsValues fromCalendarValue (final Calendar c)
    {
        return (c == null) ? null : fromCalendarValue(c.get(Calendar.MONTH));
    }

    public static final MonthsValues next (final MonthsValues cur)
    {
        if (cur == null)
            return null;

        final int    nextValue=VALUES.indexOf(cur) + 1;
        if (nextValue >= VALUES.size())
            return VALUES.get(0);
        else
            return VALUES.get(nextValue);
    }

    public static final MonthsValues prev (final MonthsValues cur)
    {
        if (cur == null)
            return null;

        final int    prevValue=VALUES.indexOf(cur) - 1;
        if (prevValue >= 0)
            return VALUES.get(prevValue);
        else
            return VALUES.get(VALUES.size() - 1);
    }

    public static final Map<MonthsValues,String> getMonthNamesMap (final DateFormatSymbols    dfs, final boolean useShortNames)
    {
        if (null == dfs)
            return null;

        final String[]    names=useShortNames ? dfs.getShortMonths() : dfs.getMonths();
        if ((null == names) || (names.length <= 0))
            return null;    // should not happen

        Map<MonthsValues,String>    namesMap=null;
        for (final MonthsValues m : VALUES)
        {
            final int        mIndex=(null == m) ? (-1) : m.getFieldValue();
            final String    n=
                ((mIndex < 0) || (mIndex >= names.length)) ? null : names[mIndex];
            if ((null == n) || (n.length() <= 0))
                continue;

            if (null == namesMap)
                namesMap = new EnumMap<MonthsValues,String>(MonthsValues.class);
            namesMap.put(m, n);
        }

        return namesMap;
    }

    public static final Map<MonthsValues,String> getMonthNamesMap (final Locale l /* null == default */, final boolean useShortNames)
    {
        return getMonthNamesMap(DateUtil.getDateFormatSymbols(l), useShortNames);
    }

    public static final Map<MonthsValues,String> getMonthNamesMap (final boolean useShortNames)
    {
        return getMonthNamesMap(Locale.getDefault(), useShortNames);
    }
}
