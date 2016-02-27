package net.community.chest.rrd4j.common.graph.helpers;

import java.util.NoSuchElementException;

import net.community.chest.dom.proxy.ReflectiveAttributesProxy;
import net.community.chest.rrd4j.common.graph.TimeAxisExt;
import net.community.chest.util.datetime.TimeUnits;

public class TimeAxisReflectiveProxy<T extends TimeAxisExt> extends ReflectiveAttributesProxy<T> {
	public TimeAxisReflectiveProxy (Class<T> objClass) throws IllegalArgumentException
	{
		super(objClass);
	}

	private TimeAxisReflectiveProxy (Class<T> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	MINOR_UNIT_ATTR="minorUnit",
								MAJOR_UNIT_ATTR="majorUnit",
								LABEL_UNIT_ATTR="labelUnit";
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#getObjectAttributeValue(java.lang.Object, java.lang.String, java.lang.String, java.lang.Class)
	 */
	@Override
	protected Object getObjectAttributeValue (T src, String name, String value, Class<?> type) throws Exception
	{
		if (MINOR_UNIT_ATTR.equalsIgnoreCase(name)
		 || MAJOR_UNIT_ATTR.equalsIgnoreCase(name)
		 || LABEL_UNIT_ATTR.equalsIgnoreCase(name))
		{
			final TimeUnits	u=TimeUnits.fromFormatChar(value);
			if (null == u)
				throw new NoSuchElementException("getObjectAttributeValue(" + name + ")[" + value + "] unknwon unit");

			return Integer.valueOf(u.getCalendarField());
		}

		return super.getObjectAttributeValue(src, name, value, type);
	}

	public static final TimeAxisReflectiveProxy<TimeAxisExt>	DEFAULT=
		new TimeAxisReflectiveProxy<TimeAxisExt>(TimeAxisExt.class, true);
}
