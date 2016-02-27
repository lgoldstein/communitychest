package net.community.chest.rrd4j.common;

import java.util.Calendar;
import java.util.Date;

import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.util.datetime.DateUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 8, 2008 2:12:58 PM
 */
public final class RrdUtils {
	private RrdUtils ()
	{
		// no instance
	}
	/**
	 * Default suffix added to classes names that extend an RRD4J class
	 */
	public static final String	DEFAULT_EXTENSION_CLASS_SUFFIX="Ext";
	/**
	 * @param elem XML {@link Element} to retrieve value from
	 * @param attrName Attribute name to use
	 * @param defValue Default value to return if no XML attribute value found
	 * @return Either retrieved value or default
	 * @throws Exception if cannot decode the retrieved XML attribute value
	 */
	public static final double getDouble (Element elem, String attrName, double defValue) throws Exception
	{
		final String	val=elem.getAttribute(attrName);
		final Double	d=((null == val) || (val.length() <= 0)) ? null : DoubleValueStringConstructor.DEFAULT.newInstance(val);
		if (null == d)
			return defValue;

		return d.doubleValue();
	}
	/**
	 * @param elem XML {@link Element} to retrieve value from
	 * @param attrName Attribute name to use
	 * @return Either retrieved value or {@link Double#NaN} if no attribute
	 * value string found
	 * @throws Exception if cannot decode the retrieved XML attribute value
	 */
	public static final double getDouble (Element elem, String attrName) throws Exception
	{
		return getDouble(elem, attrName, Double.NaN);
	}

	public static final int getInteger (Element elem, String attrName, int defValue) throws Exception
	{
		final String	val=elem.getAttribute(attrName);
		if ((null == val) || (val.length() <= 0))
			return defValue;

		return Integer.parseInt(val);
	}
	// returns Integer.MIN_VALUE if no attribute value found
	public static final int getInteger (Element elem, String attrName) throws Exception
	{
		return getInteger(elem, attrName, Integer.MIN_VALUE);
	}
	/**
	 * Converts Java msec. timestamp to RRD time units (seconds currently)
	 * @param timestamp Original (msec.) timestamp
	 * @return Adjusted timestamp to RRD units
	 */
	public static final long toRrdTime (final long timestamp)
	{
		return (timestamp + 499L) / DateUtil.MSEC_PER_SECOND;
	}

	public static final long toRrdTime (final Date time)
	{
		return (null == time) ? 0 : toRrdTime(time.getTime());
	}

	public static final long toRrdTime (final Calendar time)
	{
		return (null == time) ? 0 : toRrdTime(time.getTimeInMillis());
	}
	/**
	 * Converts RRD time units to Java msec. value
	 * @param timestamp RRD time units value
	 * @return Adjusted units to Java msec.
	 */
	public static final long fromRrdTime (final long timestamp)
	{
		return timestamp * DateUtil.MSEC_PER_SECOND;
	}
}
