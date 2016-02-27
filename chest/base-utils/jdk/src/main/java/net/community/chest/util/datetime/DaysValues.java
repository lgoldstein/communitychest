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
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 20, 2007 2:08:29 PM
 */
public enum DaysValues implements CalendarEnumFieldAccessor {
	SUNDAY(Calendar.SUNDAY),
	MONDAY(Calendar.MONDAY),
	TUESDAY(Calendar.TUESDAY),
	WEDNESDAY(Calendar.WEDNESDAY),
	THURSDAY(Calendar.THURSDAY),
	FRIDAY(Calendar.FRIDAY),
	SATURDAY(Calendar.SATURDAY);

	private final int	_fieldValue;
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
		return Calendar.DAY_OF_WEEK;
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

	DaysValues (int fieldValue)
	{
		_fieldValue = fieldValue;
	}

	public static final List<DaysValues>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static DaysValues fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static DaysValues fromCalendarValue (final int calValue)
	{
		for (final DaysValues v : VALUES)
		{
			if ((v != null) && (v.getFieldValue() == calValue))
				return v;
		}

		return null;
	}

	public static DaysValues fromCalendarValue (final Calendar c)
	{
		return (c == null) ? null : fromCalendarValue(c.get(Calendar.DAY_OF_WEEK));
	}

	public static final DaysValues next (final DaysValues cur)
	{
		if (cur == null)
			return null;

		final int	nextValue=VALUES.indexOf(cur) + 1;
		if (nextValue >= VALUES.size())
			return VALUES.get(0);
		else
			return VALUES.get(nextValue);
	}

	public static final DaysValues prev (final DaysValues cur)
	{
		if (cur == null)
			return null;

		final int	prevValue=VALUES.indexOf(cur) - 1;
		if (prevValue >= 0)
			return VALUES.get(prevValue);
		else
			return VALUES.get(VALUES.size() - 1);
	}

	public static final Map<DaysValues,String> getWeekdayNamesMap (final DateFormatSymbols dfs, final boolean useShortNames)
	{
		if (null == dfs)
			return null;

		final String[]	names=useShortNames ? dfs.getShortWeekdays() : dfs.getWeekdays();
		if ((null == names) || (names.length <= 0))
			return null;	// should not happen

		Map<DaysValues,String>	namesMap=null;
		for (final DaysValues d : VALUES)
		{
			final int		dIndex=(null == d) ? (-1) : d.getFieldValue();
			final String	n=
				((dIndex < 0) || (dIndex >= names.length)) ? null : names[dIndex];
			if ((null == n) || (n.length() <= 0))
				continue;

			if (null == namesMap)
				namesMap = new EnumMap<DaysValues,String>(DaysValues.class);
			namesMap.put(d, n);
		}

		return namesMap;
	}

	public static final Map<DaysValues,String> getWeekdayNamesMap (final Locale l /* null == default */, final boolean useShortNames)
	{
		return getWeekdayNamesMap(DateUtil.getDateFormatSymbols(l), useShortNames); 
	}

	public static final Map<DaysValues,String> getWeekdayNamesMap (final boolean useShortNames)
	{
		return getWeekdayNamesMap(Locale.getDefault(), useShortNames);
	}
}
