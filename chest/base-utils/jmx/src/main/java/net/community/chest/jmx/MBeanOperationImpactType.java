package net.community.chest.jmx;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.management.MBeanOperationInfo;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 * 
 * <P>Represent {@link MBeanOperationInfo} impact values as enum(s)</P>
 * 
 * @author Lyor G.
 * @since Aug 19, 2007 11:59:33 AM
 */
public enum MBeanOperationImpactType {
	ACTION(MBeanOperationInfo.ACTION),
	ACTION_INFO(MBeanOperationInfo.ACTION_INFO),
	INFO(MBeanOperationInfo.INFO),
	UNKNOWN(MBeanOperationInfo.UNKNOWN);
	
	private final int	_value;
	public int getImpactValue ()
	{
		return _value;
	}

	MBeanOperationImpactType (int value)
	{
		_value = value;
	}

	private static List<MBeanOperationImpactType>	_values;
	public static synchronized List<MBeanOperationImpactType> getValues ()
	{
		if (_values == null)
			_values = Collections.unmodifiableList(Arrays.asList(values()));
		return _values;
	}

	public static MBeanOperationImpactType fromString (final String s)
	{
		return CollectionsUtils.fromString(getValues(), s, false);
	}

	public static MBeanOperationImpactType fromImpact (final int n)
	{
		final Collection<MBeanOperationImpactType>	vals=getValues();
		if ((null == vals) || (vals.size() <= 0))	// should not happen
			return null;

		for (final MBeanOperationImpactType v : vals)
		{
			if ((v != null) && (v.getImpactValue() == n))
				return v;
		}

		return null;	// no match found
	}
}
