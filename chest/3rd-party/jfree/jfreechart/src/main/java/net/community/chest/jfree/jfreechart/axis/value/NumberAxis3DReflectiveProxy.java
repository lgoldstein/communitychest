/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.value;

import org.jfree.chart.axis.NumberAxis3D;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <A> Type of {@link NumberAxis3D} being reflected
 * @author Lyor G.
 * @since May 25, 2009 9:21:23 AM
 */
public class NumberAxis3DReflectiveProxy<A extends NumberAxis3D> extends NumberAxisReflectiveProxy<A> {
	protected NumberAxis3DReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public NumberAxis3DReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final NumberAxis3DReflectiveProxy<NumberAxis3D>	NUMAXIS3D=
		new NumberAxis3DReflectiveProxy<NumberAxis3D>(NumberAxis3D.class, true) {
			/*
			 * @see net.community.chest.dom.proxy.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			public NumberAxis3D createInstance (Element elem) throws Exception
			{
				final String	n=(null == elem) ? null : elem.getAttribute(NAME_ATTR);
				if ((n != null) && (n.length() > 0))
					return new NumberAxis3D(n);
				else
					return super.createInstance(elem);
			}
		};
}
