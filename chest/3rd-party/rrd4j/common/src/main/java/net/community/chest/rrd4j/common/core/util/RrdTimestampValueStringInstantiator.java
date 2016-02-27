package net.community.chest.rrd4j.common.core.util;

import java.util.Calendar;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.rrd4j.common.RrdUtils;
import net.community.chest.util.datetime.DateUtil;
import net.community.chest.util.datetime.Duration;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Converts an RRD timestamp (seconds from epoch) to/from a date/time
 * string whose format is <code>dd/mm/yyyy hh:mm:ss</code>
 * @author Lyor G.
 * @since Jan 15, 2008 11:51:47 AM
 */
public class RrdTimestampValueStringInstantiator extends AbstractXmlValueStringInstantiator<Long> {
	public RrdTimestampValueStringInstantiator ()
	{
		super(Long.class);
	}

	public String convertInstance (long ts)
	{
		if (ts <= 0L)
			return null;
		else
			return DateUtil.parseDateTimeToString(ts);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	public String convertInstance (Long inst) throws Exception
	{
		if (null == inst)
			return null;

		return convertInstance(inst.longValue());
	}

	public static final String	NOW_VALUE="now";
	public static final long convertSpecialInstanceValue (final String v)
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return (-1L);

		if (NOW_VALUE.equalsIgnoreCase(s))
			return System.currentTimeMillis();
		else
			return Duration.fromTimespec(s);
	}

	public static final long updateSpecialInstanceValue (final long accVal, final String val, final boolean positive)
	{
		final long	ts=convertSpecialInstanceValue(val);
		if (ts < 0L)
			throw new IllegalStateException("updateSpecialInstanceValue(" + val + ") illegal component");

		if (positive)
			return accVal + ts;
		else
			return accVal - ts;
	}
	/**
	 * Checks for special instances (e.g., 'now', 'now +/- 1w')
	 * @param v Original string
	 * @return Timestamp value (msec.) - non-positive if not a special value
	 */
	public static final long convertSpecialInstance (final String v)
	{
		final String	s=StringUtil.getCleanStringValue(v);
		final int		sLen=(null == s) ? 0 : s.length();
		if (sLen <= 0)
			return (-1L);

		if (NOW_VALUE.equalsIgnoreCase(s))
			return System.currentTimeMillis();

		long	accVal=0L;
		int		lastPos=0, numComps=0;
		boolean	positive=true;
		for (int	curPos=lastPos; curPos < sLen; curPos++)
		{
			final char	ch=s.charAt(curPos);
			if ((ch != '+') && (ch != '-'))
				continue;

			final String	val=s.substring(lastPos, curPos);
			accVal = updateSpecialInstanceValue(accVal, val, positive);
			numComps++;
			positive = ('+' == ch);
			lastPos = curPos + 1;
		}

		if (lastPos < sLen)
		{
			// special handling if no components restore
			if ((lastPos <= 0) && (numComps <= 0))
			{
				if (NOW_VALUE.equalsIgnoreCase(s))
					return System.currentTimeMillis();
				else	// if not 'now' then assume a real date/time value
					return (-1L);
			}

			final String	val=s.substring(lastPos);
			accVal = updateSpecialInstanceValue(accVal, val, positive);
			numComps++;
		}

		if (numComps <= 0)	// OK if nothing converted
			return (-1L);
		if (accVal <= 0L)
			throw new IllegalStateException("convertSpecialInstance(" + s + ") bad timestamp: " + accVal);

		return accVal;
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	public Long newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		long	timestamp=convertSpecialInstance(s);
		if (timestamp <= 0L)
		{
			final Calendar	c=DateUtil.parseStringToDatetime(s, true);
			timestamp = c.getTimeInMillis();
		}
		
		final long	rrdValue=RrdUtils.toRrdTime(timestamp);
		return Long.valueOf(rrdValue);
	}

	public static final RrdTimestampValueStringInstantiator	DEFAULT=new RrdTimestampValueStringInstantiator();
}
