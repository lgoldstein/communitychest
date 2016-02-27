/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.value;

import java.lang.reflect.Method;

import net.community.chest.dom.DOMUtils;
import net.community.chest.jfree.jfreechart.data.RangeValueStringInstantiator;

import org.jfree.chart.axis.ModuloAxis;
import org.jfree.data.Range;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <A> The reflected {@link ModuloAxis} type
 * @author Lyor G.
 * @since May 25, 2009 10:00:12 AM
 */
public class ModuloAxisReflectiveProxy<A extends ModuloAxis> extends NumberAxisReflectiveProxy<A> {
	protected ModuloAxisReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public ModuloAxisReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}
	public static final String	DISPRANGE_ATTR="displayRange",
								FIXEDRANGE_VIRTATTR="fixedRange";
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected A updateObjectAttribute (A src, String name, String value, Method setter) throws Exception
	{
		if (FIXEDRANGE_VIRTATTR.equalsIgnoreCase(name))
			return src;	// used in the constructor
		else if (DISPRANGE_ATTR.equalsIgnoreCase(name))
		{
			final Range	r=RangeValueStringInstantiator.fromString(value);
			src.setDisplayRange(r.getLowerBound(), r.getUpperBound());
			return src;
		}

		return super.updateObjectAttribute(src, name, value, setter);
	}

	public static final ModuloAxisReflectiveProxy<ModuloAxis>	MODULO=
		new ModuloAxisReflectiveProxy<ModuloAxis>(ModuloAxis.class, true) {
			/*
			 * @see net.community.chest.dom.proxy.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			public ModuloAxis createInstance (Element elem) throws Exception
			{
				final String	n=elem.getAttribute(NAME_ATTR),
								v=elem.getAttribute(FIXEDRANGE_VIRTATTR);
				final Range		r=RangeValueStringInstantiator.fromString(v);
				if (null == r)
					throw new IllegalStateException("createInstance(" + DOMUtils.toString(elem) + ") no '" + FIXEDRANGE_VIRTATTR + "' specification");

				return new ModuloAxis(n, r);
			}
		};
}
