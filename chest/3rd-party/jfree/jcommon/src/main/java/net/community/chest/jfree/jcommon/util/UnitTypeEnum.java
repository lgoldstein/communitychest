/*
 * 
 */
package net.community.chest.jfree.jcommon.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.util.UnitType;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates the {@link UnitType} as an {@link Enum}</P>
 * @author Lyor G.
 * @since Jan 27, 2009 4:10:34 PM
 */
public enum UnitTypeEnum {
	ABSOLUTE(UnitType.ABSOLUTE),
	RELATIVE(UnitType.RELATIVE);

	private final UnitType	_type;
	public final UnitType getUnitType ()
	{
		return _type;
	}

	UnitTypeEnum (UnitType t)
	{
		_type = t;
	}

	public static final List<UnitTypeEnum>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final UnitTypeEnum fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final UnitTypeEnum fromUnitType (final UnitType t)
	{
		if (null == t)
			return null;

		for (final UnitTypeEnum v : VALUES)
		{
			if ((v != null) && t.equals(v.getUnitType()))
				return v;
		}

		return null;
	}
}
