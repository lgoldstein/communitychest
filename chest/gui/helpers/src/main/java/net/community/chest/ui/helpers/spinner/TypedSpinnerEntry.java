/*
 * 
 */
package net.community.chest.ui.helpers.spinner;

import java.util.Map;

import net.community.chest.reflect.ClassUtil;
import net.community.chest.ui.helpers.combobox.TypedComboBoxEntry;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> The contained value type
 * @author Lyor G.
 * @since Dec 16, 2008 2:07:23 PM
 */
public class TypedSpinnerEntry<V> extends TypedComboBoxEntry<V> {
	public TypedSpinnerEntry ()
	{
		super();
	}

	public TypedSpinnerEntry (Map.Entry<String,V> e)
	{
		super(e);
	}

	public TypedSpinnerEntry (String key, V value)
	{
		super(key, value);
	}

	public TypedSpinnerEntry (String key)
	{
		super(key);
	}
	/*
	 * @see net.community.chest.util.map.MapEntryImpl#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (final Object obj)
	{
		if (this == obj)
			return true;

		final Object	ov;
		if (obj instanceof Map.Entry<?,?>)
		{
			final Map.Entry<?,?>	e=(Map.Entry<?,?>) obj;
			ov = e.getValue();
		}
		else
			ov = obj;

		final Object tv=getAssignedValue();
		if (tv == ov)
			return true;
		else if (null == tv)
			return false;
		else
			return tv.equals(ov);
	}
	/*
	 * @see net.community.chest.util.map.MapEntryImpl#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return ClassUtil.getObjectHashCode(getAssignedValue());
	}
}
