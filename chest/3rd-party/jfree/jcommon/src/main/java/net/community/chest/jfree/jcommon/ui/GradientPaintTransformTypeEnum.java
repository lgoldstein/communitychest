/*
 * 
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.ui.GradientPaintTransformType;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulates the {@link GradientPaintTransformType} values as a proper
 * {@link Enum}</P>
 * @author Lyor G.
 * @since Jun 8, 2009 1:35:14 PM
 */
public enum GradientPaintTransformTypeEnum {
	VERTICAL(GradientPaintTransformType.VERTICAL), 
	HORIZONTAL(GradientPaintTransformType.HORIZONTAL), 
	VCENTER(GradientPaintTransformType.CENTER_VERTICAL), 
	HCENTER(GradientPaintTransformType.CENTER_HORIZONTAL);

	private final GradientPaintTransformType	_type;
	public final GradientPaintTransformType getType ()
	{
		return _type;
	}

	GradientPaintTransformTypeEnum (GradientPaintTransformType t)
	{
		_type = t;
	}

	public static final List<GradientPaintTransformTypeEnum>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final GradientPaintTransformTypeEnum fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final GradientPaintTransformTypeEnum fromType (final GradientPaintTransformType t)
	{
		if (null == t)
			return null;

		for (final GradientPaintTransformTypeEnum  v : VALUES)
		{
			if ((v != null) && t.equals(v.getType()))
				return v;
		}

		return null;
	}
}
