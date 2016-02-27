/*
 * 
 */
package net.community.chest.jfree.jfreechart.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jfree.data.Values;
import org.jfree.data.general.Series;
import org.jfree.data.time.TimeSeries;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 2, 2010 1:37:21 PM
 */
public final class DatasetUtils {
	private DatasetUtils ()
	{
		// no instance
	}
	/**
	 * @param vals A {@link Values} implementation
	 * @return A {@link List} of all {@link Number}-s in the {@link Values} - may
	 * be null/empty if no values
	 */
	public static final List<Number> extractValues (final Values vals)
	{
		final int	numValues=(null == vals) ? 0 : vals.getItemCount();
		if (numValues <= 0)
			return null;
		
		final List<Number>	ret=new ArrayList<Number>(numValues);
		for (int	vIndex=0; vIndex < numValues; vIndex++)
		{
			final Number	n=vals.getValue(vIndex);
			if (null == n)
				continue;
			if (!ret.add(n))
				continue;
		}
		
		return ret;
	}
	/**
	 * @param s A {@link TimeSeries} instance
	 * @return A {@link List} of all {@link Number}-s in the {@link TimeSeries} - may
	 * be null/empty if no values
	 */
	public static final List<Number> extractValues (final TimeSeries s)
	{
		final int	numValues=(null == s) ? 0 : s.getItemCount();
		if (numValues <= 0)
			return null;

		final List<Number>	ret=new ArrayList<Number>(numValues);
		for (int	vIndex=0; vIndex < numValues; vIndex++)
		{
			final Number	n=s.getValue(vIndex);
			if (null == n)
				continue;
			if (!ret.add(n))
				continue;
		}
		
		return ret;
	}
	/**
	 * @param <S> Type of {@link Series} contained in the {@link Collection}
	 * @param keyValue The {@link Comparable} key value to look-up
	 * @param sl A {@link Collection} of {@link Series}-derived objects whose
	 * {@link Series#getKey()} we want check if same as the key value
	 * @return The first matching value - null if no match (or no key, or
	 * null/empty collection);
	 */
	public static final <S extends Series> S findSeriesByKey (
			final Comparable<?> keyValue, final Collection<? extends S> sl)
	{
		if ((null == keyValue) || (null == sl) || (sl.size() <= 0))
			return null;

		for (final S s : sl)
		{
			final Comparable<?>	sKey=(null == s) ? null : s.getKey();
			if (null == sKey)
				continue;
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final int	nRes=((Comparable) keyValue).compareTo(sKey);
			if (0 == nRes)
				return s;
		}
		
		return null;
	}
}
