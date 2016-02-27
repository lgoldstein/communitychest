/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.category;

import org.jfree.chart.axis.SubCategoryAxis;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <A> Type of {@link SubCategoryAxis} being reflected
 * @author Lyor G.
 * @since May 5, 2009 2:18:52 PM
 */
public class SubCategoryAxisReflectiveProxy<A extends SubCategoryAxis>
			extends CategoryAxisReflectiveProxy<A> {
	protected SubCategoryAxisReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public SubCategoryAxisReflectiveProxy (Class<A> objClass)
			throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final SubCategoryAxisReflectiveProxy<SubCategoryAxis>	SUBCAT=
		new SubCategoryAxisReflectiveProxy<SubCategoryAxis>(SubCategoryAxis.class, true);
}
