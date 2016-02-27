/*
 * 
 */
package net.community.chest.lang.math;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.map.BooleansMap;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Holds enumeration values for the size units &quot;hierarchy&quot;</P>
 * @author Lyor G.
 * @since Sep 22, 2008 8:47:50 AM
 */
public enum SizeUnits {
	B("B", 1L),
	KB("KB", B),
	MB("MB", KB),
	GB("GB", MB),
	TB("TB", GB),
	PB("PB", TB),
	HB("HB", PB);	// 62 bits representation - max. possible with long

	private final String	_sizeName;
	/**
	 * @return Suffix used to denote a unit
	 */
	public final String getSizeName ()
	{
		return _sizeName;
	}

	private final long	_mulFactor;
	/**
	 * @return Number of bytes representing <U>one</U> unit
	 */
	public final long getMultiplicationFactor ()
	{
		return _mulFactor;
	}

	public long getSizeValue (long numUnits)
	{
		return numUnits * getMultiplicationFactor();
	}
	/**
	 * Converts from another unit into this one
	 * @param numUnits Number of {@link SizeUnits} of the &quot;other&quot;
	 * unit
	 * @param unit The &quot;other&quot; unit
	 * @return The number of units required to represent the &quot;other&quot;
	 * unit into &quot;this&quot; one
	 * @throws IllegalArgumentException if no &quot;other&quot; unit instance
	 * provided
	 */
	public double convertToThisUnit (final double numUnits, final SizeUnits unit) throws IllegalArgumentException
	{
		if (null == unit)
			throw new IllegalArgumentException("convertToThisUnit(" + numUnits + ") no other unit specified");

		final long	thisFactor=getMultiplicationFactor(), otherFactor=unit.getMultiplicationFactor();
		if (thisFactor == otherFactor)
			return numUnits;

		final double	unitFactor=(double) otherFactor / (double) thisFactor;
		return unitFactor * numUnits;
	}
	/**
	 * Converts a size in <U>bytes</U> into the best unit/value &quot;pair&quot;
	 * @param sz Size in <U>bytes</U> - may not be negative
	 * @return The closest unit/value &quot;pairs&quot; represented as a {@link Map}
	 * whose key={@link SizeUnits} value, value=number of such units required
	 * to reach/add the specified size
	 * @throws NumberFormatException if negative value provided
	 */
	public static final Map<SizeUnits,Long> fromSize (final long sz) throws NumberFormatException
	{
		if (sz < 0L)
			throw new NumberFormatException("fromSize(" + sz + ") negative values N/A");

		final Map<SizeUnits,Long>	szMap=new EnumMap<SizeUnits,Long>(SizeUnits.class);
		// shortcut (covers ZERO as well)
		if (sz < KB.getMultiplicationFactor())
		{
			szMap.put(SizeUnits.B, Long.valueOf(sz));
			return szMap;
		}

		// we go "downwards" in the sizes
		final Collection<SizeUnits>	ua=SizeUnits.getValues(Boolean.FALSE);
		long						remSize=sz;
		for (final SizeUnits u : ua)
		{
			final long	mulFactor=(null == u) ? 0L : u.getMultiplicationFactor();
			if ((mulFactor <= 0L)			// should not happen
			 || (mulFactor > remSize))		// means have less than 1 unit
				continue;

			final long	mulUnit=remSize / mulFactor;
			szMap.put(u, Long.valueOf(mulUnit));

			if (0 == (remSize %= mulFactor))
				break; 
		}

		if (remSize > 0L)	// should not happen
			throw new NumberFormatException("fromSize(" + sz + ") incomplete conversion");

		return szMap;
	}

	SizeUnits (final String sizeName, final long mulFactor)
	{
		_sizeName = sizeName;
		_mulFactor = mulFactor;
	}

	SizeUnits (final String sizeName, final SizeUnits subUnit)
	{
		this(sizeName, subUnit.getMultiplicationFactor() * 1024L);
	}

	@SuppressWarnings({ "cast", "unchecked", "rawtypes" })
	private static final BooleansMap<List<SizeUnits>>	_unitsMap=(BooleansMap<List<SizeUnits>>) new BooleansMap(List.class, true);
	/**
	 * Returns a (cached) array of {@link SizeUnits} sorted by multiplication
	 * factor according to the provided parameter
	 * @param ascending Sort direction (<code>null</code> means un-sorted)
	 * @return A {@link List} of {@link SizeUnits} sorted by multiplication factor
	 * (if non-<code>null</code> sort direction specified)
	 */
	public static final List<SizeUnits> getValues (final Boolean  ascending /* null == unsorted */)
	{
		synchronized(_unitsMap) {
			List<SizeUnits>	vl=_unitsMap.get(ascending);
			if (null == vl)
			{
				final SizeUnits[]	va=values();

				if (ascending != null)
				{
					final Comparator<SizeUnits>	c=ascending.booleanValue()
						? ByFactorSizeUnitsComparator.ASCENDING
						: ByFactorSizeUnitsComparator.DESCENDING;
					Arrays.sort(va, c);
				}

				vl = Collections.unmodifiableList(Arrays.asList(va));
				_unitsMap.put(ascending, vl);
			}

			return vl;
		}
	}

	public static final List<SizeUnits> getValues ()
	{
		return getValues(null);
	}

	public static final SizeUnits fromString (final String s)
	{
		return CollectionsUtils.fromString(getValues(), s, false);
	}

	public static final SizeUnits fromSizeName (final String n)
	{
		if ((null == n) || (n.length() <= 0))
			return null;

		final Collection<SizeUnits>	vals=getValues();
		if ((null == vals) || (vals.size() <= 0))
			return null;	// should not happen

		for (final SizeUnits v : vals)
		{
			final String	vn=(null == v) ? null : v.getSizeName();
			if (0 == StringUtil.compareDataStrings(n, vn, false))
				return v;
		}

		return null;
	}

	public static final SizeUnits fromSizeChar (final char c)
	{
		final Collection<SizeUnits>	vals=getValues();
		if ((null == vals) || (vals.size() <= 0))
			return null;	// should not happen

		final char	cc=((c >= 'a') && (c <= 'z')) ? Character.toUpperCase(c) : c;
		for (final SizeUnits v : vals)
		{
			final String	vn=(null == v) ? null : v.getSizeName();
			if ((vn != null) && (vn.length() > 0) && (vn.charAt(0) == cc))
				return v;
		}

		return null;
	}
	// format: 1G2M3K4B
	public static final Map<SizeUnits,Long> fromSizeString (final CharSequence cs) throws NumberFormatException
	{
		final int			csLen=(null == cs) ? 0 : cs.length();
		int					lastPos=0;
		Map<SizeUnits,Long>	ret=null;
		for (int curPos=0; curPos < csLen; curPos++)
		{
			final char	c=cs.charAt(curPos);
			if ((c >= '0') && (c <= '9'))
				continue;

			if (curPos <= lastPos)
				throw new NumberFormatException("fromSizeString(" + cs + ") no number before '" + String.valueOf(c) + "' unit specifier");

			final SizeUnits	u=fromSizeChar(c);
			if (null == u)
				throw new NumberFormatException("fromSizeString(" + cs + ") unknown unit: " + String.valueOf(c));

			final CharSequence	vs=cs.subSequence(lastPos, curPos);
			final Long			v=Long.valueOf(vs.toString()),
								p=(null == ret) ? null : ret.get(u);
			if (p != null)	// if have a previous value for the same unit then add them
			{
				ret.put(u, Long.valueOf(v.longValue() + p.longValue()));
				continue;
			}
	
			if (null == ret)
				ret = new EnumMap<SizeUnits,Long>(SizeUnits.class);
			ret.put(u, v);
		}

		return ret;
	}
	// NOTE: overflow may occur for high memory sizes 
	public static final double fromSizeUnits (final Collection<? extends Map.Entry<SizeUnits,? extends Number>> sl, final SizeUnits targetUnit)
	{
		if ((null == sl) || (sl.size() <= 0) || (null == targetUnit))
			return 0.0d;

		double	ret=0.0d;
		for (final Map.Entry<SizeUnits,? extends Number>	se : sl)
		{
			final SizeUnits	u=(null == se) ? null : se.getKey();
			final Number	n=(null == se) ? null : se.getValue();
			if ((null == u) || (null == n) || (0L == n.longValue()))
				continue;

			final double	v=targetUnit.convertToThisUnit(n.doubleValue(), u);
			if (v <= 0.0d)
				continue;

			ret += v;
		}

		return ret;
	}
	// NOTE: overflow may occur for high memory sizes 
	public static final double fromSizeUnits (final Map<SizeUnits,? extends Number> m, final SizeUnits targetUnit)
	{
		return fromSizeUnits(((null == m) || (m.size() <= 0)) ? null : m.entrySet(), targetUnit);
	}
	// format: 1G2M3K4B
	public static final double fromSizeString (final CharSequence cs, final SizeUnits targetUnit) throws NumberFormatException
	{
		final Map<SizeUnits,? extends Number> m=(null == targetUnit) ? null : fromSizeString(cs);
		return fromSizeUnits(m, targetUnit);
	}
 }