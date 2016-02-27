package net.community.chest.apache.log4j.helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Class that can be used to convert date/time values. <B>Note:</B> the
 * <I>log4j</I> implementation has a much richer date/time converter, but
 * it is <I>private</I> and formats only the <U>logging event's</U> date/time
 * value(s) - whereas we might need such formatting for some other purposes.
 * (TODO review this class if/when the internal <I>log4j</I> converter
 * becomes <I>public</I>).</P>
 * 
 * @author Lyor G.
 * @since Sep 26, 2007 12:15:53 PM
 */
public class DateTimeConverter extends PatternConverter {
	private DateFormat	_dtf	/* =null */;
	public DateFormat getFormat ()
	{
		return _dtf;
	}

	public void setFormat (DateFormat dtf)
	{
		_dtf = dtf;
	}
	/**
	 * Builds a date/time converter that can use any of the
	 * {@link SimpleDateFormat} formats.
	 * @param fmt format string to use - if <I>null</I>/empty, then
	 * default for the current locale is used (and the locale parameter
	 * if any, is <U>ignored</U>)
	 * @param l {@link Locale} object to be used - if <I>null</I> then
	 * default locale is used
	 */
	public DateTimeConverter (final String fmt, final Locale l)
	{
		if ((null == fmt) || (fmt.length() <= 0))
			_dtf = new SimpleDateFormat();
		else if (null == l)
			_dtf = new SimpleDateFormat(fmt);
		else
			_dtf = new SimpleDateFormat(fmt, l);
	}
	/**
	 * Builds a date/time converter that can use any of the
	 * {@link SimpleDateFormat} formats.
	 * @param fmt format string to use - if <I>null</I>/empty, then
	 * default for the current locale is used
	 * @see SimpleDateFormat
	 */
	public DateTimeConverter (final String fmt)
	{
		this(fmt, Locale.getDefault());
	}
	/**
	 * Builds a date/time converter that use the defaults for the current locale
	 * @see #DateTimeConverter(String)
	 */
	public DateTimeConverter ()
	{
		this(null);
	}
	/**
	 * Formats the supplied {@link Calendar} value according to the internal
	 * date/time format
	 * @param cal {@link Calendar} value to be formatted
	 * @return formatted string - or <I>null</I> if no value/format available
	 */
	protected String formatValue (final Calendar cal)
	{
		final Date			dtv=(null == cal) ? null : cal.getTime();
		final DateFormat	dtf=(null == dtv) ? null : getFormat();
		if (null == dtf)
			return null;

		synchronized(dtf)
		{
			return dtf.format(dtv);
		}
	}
	/*
	 * @see org.apache.log4j.helpers.PatternConverter#convert(org.apache.log4j.spi.LoggingEvent)
	 */
	@Override
	protected String convert (LoggingEvent event)
	{
		if (null == event)
			return null;

		final long		tValue=event.getTimeStamp();
		final Calendar	cal=Calendar.getInstance();
		cal.setTimeInMillis(tValue);
		return formatValue(cal);
	}
}
