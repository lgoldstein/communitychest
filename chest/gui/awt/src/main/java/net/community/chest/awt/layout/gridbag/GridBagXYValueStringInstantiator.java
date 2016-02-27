/*
 * 
 */
package net.community.chest.awt.layout.gridbag;

import java.awt.GridBagConstraints;

import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.NumberValueStringConstructor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 8, 2009 8:26:27 AM
 */
public class GridBagXYValueStringInstantiator extends NumberValueStringConstructor<Integer> {
	public GridBagXYValueStringInstantiator ()
	{
		super(Integer.TYPE, Integer.class);
	}
	/**
	 * Special string used to detect (case insensitive) a relative XY
	 * specification
	 */
	public static final String	RELATIVE_VALUE="relative";
	/**
	 * @param attrVal A {@link String} value containing the <I>gridx/gridy</U>
	 * value either as a number or as the special {@link #RELATIVE_VALUE}.
	 * @return converted value - null if null/empty value
	 * @throws NumberFormatException if bad format of data (e.g., non-numerical and
	 * not the special {@link #RELATIVE_VALUE} string)
	 */
	public static final Integer getGridXYValue (final String attrVal)
	{
		if ((null == attrVal) || (attrVal.length() <= 0))
			return null;

		// check special case of "relative" specification
		if (RELATIVE_VALUE.equalsIgnoreCase(attrVal))
			return Integer.valueOf(GridBagConstraints.RELATIVE);

		return Integer.valueOf(attrVal);
	}
	/*
	 * @see net.community.chest.reflect.ValueStringConstructor#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (final Integer inst) throws Exception
	{
		if (null == inst)
			return null;

		if (inst.intValue() == GridBagConstraints.RELATIVE)
			return RELATIVE_VALUE;

		return super.convertInstance(inst);
	}
	/*
	 * @see net.community.chest.reflect.ValueStringConstructor#newInstance(java.lang.String)
	 */
	@Override
	public Integer newInstance (final String v) throws Exception
	{
		// check special case of "relative" specification
		final String	s=StringUtil.getCleanStringValue(v);
		if (RELATIVE_VALUE.equalsIgnoreCase(s))
			return Integer.valueOf(GridBagConstraints.RELATIVE);

		return super.newInstance(s);
	}
}
