/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.category;

import org.jfree.chart.axis.ExtendedCategoryAxis;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <A> Type of {@link ExtendedCategoryAxis} being reflected
 * @author Lyor G.
 * @since May 5, 2009 2:14:25 PM
 */
public class ExtendedCategoryAxisReflectiveProxy<A extends ExtendedCategoryAxis>
				extends CategoryAxisReflectiveProxy<A> {
	protected ExtendedCategoryAxisReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public ExtendedCategoryAxisReflectiveProxy (Class<A> objClass)
			throws IllegalArgumentException
	{
		this(objClass, false);
	}
	
	public static final ExtendedCategoryAxisReflectiveProxy<ExtendedCategoryAxis>	EXTCAT=
		new ExtendedCategoryAxisReflectiveProxy<ExtendedCategoryAxis>(ExtendedCategoryAxis.class, true) {
			/*
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			public ExtendedCategoryAxis createInstance (Element elem) throws Exception
			{
				final String	n=elem.getAttribute(NAME_ATTR);
				return new ExtendedCategoryAxis(n);
			}
		};
}
