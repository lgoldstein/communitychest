/*
 * 
 */
package net.community.chest.util.datetime;

import java.util.Calendar;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses ISO8601 format</P>
 * 
 * @param <C> The instantiated {@link Calendar} value
 * @author Lyor G.
 * @since Dec 14, 2008 2:39:23 PM
 */
public class CalendarValueInstantiator<C extends Calendar> extends AbstractXmlValueStringInstantiator<C> {
	public CalendarValueInstantiator (Class<C> objClass) throws IllegalArgumentException
	{
		super(objClass);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (C inst) throws Exception
	{
		return (null == inst) ? null : DateUtil.toISO8601(inst);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public C newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final C	cal=getValuesClass().newInstance();
		return DateUtil.fromISO8601(cal, s);
	}

	public static final CalendarValueInstantiator<Calendar>	DEFAULT=
				new CalendarValueInstantiator<Calendar>(Calendar.class) {
			/*
			 * @see net.community.chest.util.datetime.CalendarValueInstantiator#newInstance(java.lang.String)
			 */
			@Override
			public Calendar newInstance (String v) throws Exception
			{
				final String	s=StringUtil.getCleanStringValue(v);
				if ((null == s) || (s.length() <= 0))
					return null;
	
				return DateUtil.fromISO8601(s);
			}
		};
}
