/*
 * 
 */
package net.community.chest.util.datetime;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>An {@link Enum} used to encapsulate the {@link Calendar} fields</P>
 * 
 * @author Lyor G.
 * @since Oct 15, 2008 4:14:22 PM
 */
public enum CalendarFieldType implements CalendarFieldSetter {
	ERA(Calendar.ERA),
	YEAR(Calendar.YEAR),
	MONTH(Calendar.MONTH),
	WOY(Calendar.WEEK_OF_YEAR),
	WOM(Calendar.WEEK_OF_MONTH),
//	DATE(Calendar.DATE),	// same as DAY 
	DAY(Calendar.DAY_OF_MONTH),
	DOY(Calendar.DAY_OF_YEAR),
	DOW(Calendar.DAY_OF_WEEK),
	DOWM(Calendar.DAY_OF_WEEK_IN_MONTH),
	AMPM(Calendar.AM_PM),
	H1224(Calendar.HOUR),
	HOUR(Calendar.HOUR_OF_DAY),
	MINUTE(Calendar.MINUTE),
	SECOND(Calendar.SECOND),
	MSEC(Calendar.MILLISECOND),
	ZOFFSET(Calendar.ZONE_OFFSET),
	DOFFSET(Calendar.DST_OFFSET);

	private final int	_fieldId;
	/*
	 * @see net.community.chest.util.datetime.CalendarFieldIndicator#getCalendarValue()
	 */
	@Override
	public final int getCalendarFieldId ()
	{
		return _fieldId;
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
	 * @see net.community.chest.util.datetime.CalendarFieldSetter#setFieldValue(java.util.Calendar, int)
	 */
	@Override
	public void setFieldValue (Calendar c, int value)
	{
		if (c == null)
			return;	// debug breakpoint

		c.set(getCalendarFieldId(), value);
	}

	// TODO review this code if new JDK version
	private static Method	_nm	/* =null */;
	private static final String getFieldName (final int id)
	{
		if ((id < 0) || (id >= Calendar.FIELD_COUNT))
			return null;

		try
		{
			if (null == _nm)
				_nm = Calendar.class.getDeclaredMethod("getFieldName", Integer.TYPE);

			return (String) _nm.invoke(null, Integer.valueOf(id));
		}
		catch(Exception e)
		{
			return null;
		}
	}

	private final String	_name;
	public final String getFieldName ()
	{
		return _name;
	}

	CalendarFieldType (int id)
	{
		_fieldId = id;
		_name = getFieldName(id);
	}

	public static final List<CalendarFieldType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final CalendarFieldType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final CalendarFieldType fromFieldId (final int id)
	{
		if ((id < 0) || (id >= Calendar.FIELD_COUNT))
			return null;

		for (final CalendarFieldType v : VALUES)
		{
			if ((v != null) && (v.getCalendarFieldId() == id))
				return v;
		}

		return null;
	}

	public static final CalendarFieldType fromFieldName (final String n)
	{
		if ((null == n) || (n.length() <= 0))
			return null;

		for (final CalendarFieldType v : VALUES)
		{
			final String	vn=(null == v) ? null : v.getFieldName();
			if (n.equalsIgnoreCase(vn))
				return v;
		}

		return null;
	}
}
