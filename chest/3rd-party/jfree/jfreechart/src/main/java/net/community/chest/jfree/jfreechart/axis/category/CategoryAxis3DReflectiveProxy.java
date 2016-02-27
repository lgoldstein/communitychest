/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.category;

import org.jfree.chart.axis.CategoryAxis3D;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <A> The reflected {@link CategoryAxis3D} type
 * @author Lyor G.
 * @since May 5, 2009 2:43:33 PM
 */
public class CategoryAxis3DReflectiveProxy<A extends CategoryAxis3D> extends CategoryAxisReflectiveProxy<A> {
	protected CategoryAxis3DReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public CategoryAxis3DReflectiveProxy (Class<A> objClass)
			throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final CategoryAxis3DReflectiveProxy<CategoryAxis3D>	CAT3D=
		new CategoryAxis3DReflectiveProxy<CategoryAxis3D>(CategoryAxis3D.class, true);
}
