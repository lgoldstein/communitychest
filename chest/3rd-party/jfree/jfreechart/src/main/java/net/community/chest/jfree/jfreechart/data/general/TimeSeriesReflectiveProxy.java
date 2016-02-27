/*
 * 
 */
package net.community.chest.jfree.jfreechart.data.general;

import net.community.chest.dom.DOMUtils;

import org.jfree.data.time.TimeSeries;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <S> The reflected {@link TimeSeries}
 * @author Lyor G.
 * @since Feb 11, 2009 12:50:19 PM
 */
public class TimeSeriesReflectiveProxy<S extends TimeSeries> extends SeriesReflectiveProxy<S> {
	protected TimeSeriesReflectiveProxy (Class<S> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public TimeSeriesReflectiveProxy (Class<S> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final Comparable<?> getName (Element elem, String aName) throws Exception
	{
		final String	value=
			((null == elem) || (null == aName) || (aName.length() <= 0)) ? null : elem.getAttribute(aName);
		if ((null == value) || (value.length() <= 0))
			return null;

		return TimeSeriesReflectiveProxy.TIMESERIES.getKeyValue(aName, value);
	}

	public static final Comparable<?> getName (Element elem) throws Exception
	{
		return getName(elem, NAME_ATTR);
	}

	public static final TimeSeriesReflectiveProxy<TimeSeries>	TIMESERIES=
			new TimeSeriesReflectiveProxy<TimeSeries>(TimeSeries.class, true) {
				/*
				 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
				 */
				@Override
				public TimeSeries createInstance (Element elem) throws Exception
				{
					final Comparable<?>	name=getName(elem);
					if (null == name)
						throw new IllegalArgumentException("createInstance(" + DOMUtils.toString(elem) + ") missing name");
					return new TimeSeries(name);
				}
		};
}
