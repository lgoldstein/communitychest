/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.category;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.ExtendedCategoryAxis;
import org.jfree.chart.axis.SubCategoryAxis;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulates the various {@link CategoryAxis} available</P>
 * 
 * @author Lyor G.
 * @since May 5, 2009 2:25:53 PM
 */
public enum CategoryAxisType {
	DEFAULT(CategoryAxis.class),
	THREEDI(CategoryAxis3D.class),
	EXTENDED(ExtendedCategoryAxis.class),
	SUB(SubCategoryAxis.class);

	private final Class<? extends CategoryAxis>	_c;
	public final Class<? extends CategoryAxis> getAxisClass ()
	{
		return _c;
	}
	
	CategoryAxisType (final Class<? extends CategoryAxis> c)
	{
		_c = c;
	}

	public static final List<CategoryAxisType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final CategoryAxisType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final CategoryAxisType fromClass (final Class<?> c)
	{
		if ((null == c) || (!CategoryAxis.class.isAssignableFrom(c)))
			return null;

		CategoryAxisType			ret=null;
		for (final CategoryAxisType v : VALUES)
		{
			final Class<?>	vc=(null == v) ? null : v.getAxisClass();
			if ((null == vc) || (!vc.isAssignableFrom(c)))
				continue;
	
			// look for most specific match since some are derived classes
			if (null == ret)
			{
				ret = v;
				continue;
			}

			final Class<?>	rc=ret.getAxisClass();
			if (!rc.isAssignableFrom(vc))
				continue;

			ret = v;
		}

		return ret;
	}
	
	public static final CategoryAxisType fromObject (Object o)
	{
		if (o instanceof CategoryAxis)
			return fromClass(o.getClass());
		else	// debug breakpoint
			return null;
	}
}
