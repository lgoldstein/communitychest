/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.category;

import java.lang.reflect.Constructor;
import java.util.NoSuchElementException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.jfree.jfreechart.axis.AxisReflectiveProxy;

import org.jfree.chart.axis.CategoryAxis;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <A> The reflected {@link CategoryAxis} instance
 * @author Lyor G.
 * @since Feb 5, 2009 3:27:35 PM
 */
public class CategoryAxisReflectiveProxy<A extends CategoryAxis> extends AxisReflectiveProxy<A> {
	protected CategoryAxisReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
	
	public CategoryAxisReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final CategoryAxis createCategoryAxisFromElement (Element elem) throws Exception
	{
		final String	type=(null == elem) ? null : elem.getAttribute(CLASS_ATTR);
		if ((null == type) || (type.length() <= 0))
			return null;

		final CategoryAxisType	t=CategoryAxisType.fromString(type);
		if (null == t)
			throw new NoSuchElementException("createCategoryAxisFromElement(" + DOMUtils.toString(elem) + ") unknown axis type: " + type);

		final Class<? extends CategoryAxis>			c=t.getAxisClass();
		// all of them have a constructor with a String argument, but not all have a no-args one
		final Constructor<? extends CategoryAxis>	x=c.getConstructor(String.class);
		return x.newInstance(type);
	}

	public static final CategoryAxisReflectiveProxy<CategoryAxis>	CATEGORY=
			new CategoryAxisReflectiveProxy<CategoryAxis>(CategoryAxis.class, true) {
				/*
				 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
				 */
				@Override
				public CategoryAxis createInstance (Element elem) throws Exception
				{
					final CategoryAxis	c=createCategoryAxisFromElement(elem);
					if (null == c)
						return super.createInstance(elem);
					else
						return c;
				}
		};
}
