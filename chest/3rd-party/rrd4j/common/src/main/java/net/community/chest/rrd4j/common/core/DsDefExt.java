package net.community.chest.rrd4j.common.core;

import net.community.chest.rrd4j.common.DsTypeExt;
import net.community.chest.rrd4j.common.RrdUtils;
import net.community.chest.util.datetime.Duration;
import net.community.chest.util.datetime.TimeUnits;

import org.rrd4j.DsType;
import org.rrd4j.core.DsDef;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 8, 2008 12:57:01 PM
 */
public class DsDefExt extends DsDef {
	public static final int MAX_NAME_LENGTH=20;	// NOTE !!! must match RrdPrimitive.STRING_LENGTH

	public DsDefExt (String dsName, DsType dsType, long heartbeat, double minValue, double maxValue)
	{
		super(dsName, dsType, heartbeat, minValue, maxValue);
	}

	public DsDefExt (String dsName, DsType dsType, TimeUnits htUnits, long htValue, double minValue, double maxValue)
	{
		this(dsName, dsType, RrdUtils.toRrdTime(htUnits.getMilisecondValue(htValue)), minValue, maxValue);
	}

	public DsDefExt (String dsName, DsType dsType, long heartbeat)
	{
		this(dsName, dsType, heartbeat, Double.NaN, Double.NaN);
	}

	public DsDefExt (String dsName, DsType dsType, TimeUnits htUnits, long htValue)
	{
		this(dsName, dsType, htUnits, htValue, Double.NaN, Double.NaN);
	}

	public static final String	DSDEF_ELEM_NAME=DsDef.class.getSimpleName(),
									DSNAME_ATTR="dsName",
									HEARTBEAT_ATTR="heartbeat",
									MIN_ATTR="min", MAX_ATTR="max";
	public static final String getDsName (final Element elem)
	{
		return elem.getAttribute(DSNAME_ATTR);
	}

	public static final Long getHeartBeatValue (Element elem) throws Exception
	{
		final String	val=elem.getAttribute(HEARTBEAT_ATTR);
		if ((null == val) || (val.length() <= 0))
			return null;

		// heart-beat specification is in seconds
		final long	ts=RrdUtils.toRrdTime(Duration.fromTimespec(val));
		if (ts < 0L)
			throw new IllegalArgumentException("Bad heartbeat specification: " + val);

		return Long.valueOf(ts);
	}

	public static final long getHeartBeat (Element elem) throws Exception
	{
		final Long	v=getHeartBeatValue(elem);
		if (null == v)
			return -1L;

		return v.longValue();
	}

	public static final double getMinValue (Element elem) throws Exception
	{
		return RrdUtils.getDouble(elem, MIN_ATTR);
	}

	public static final double getMaxValue (Element elem) throws Exception
	{
		return RrdUtils.getDouble(elem, MAX_ATTR);
	}

	public DsDefExt (Element elem) throws Exception
	{
		this(getDsName(elem), DsTypeExt.DEFAULT.fromXml(elem), getHeartBeat(elem), getMinValue(elem), getMaxValue(elem));
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return dump();
	}
}
