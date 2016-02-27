/*
 * 
 */
package net.community.chest.text;

import java.text.DecimalFormat;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link DecimalFormat}
 * @author Lyor G.
 * @since Jan 12, 2009 3:23:33 PM
 */
public class DecimalFormatReflectiveProxy<F extends DecimalFormat> extends NumberFormatReflectiveProxy<F> {
	protected DecimalFormatReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public DecimalFormatReflectiveProxy (Class<F> objClass)
	{
		this(objClass, false);
	}

	public static final DecimalFormatReflectiveProxy<DecimalFormat>	DECFMT=
		new DecimalFormatReflectiveProxy<DecimalFormat>(DecimalFormat.class, true) {
			/*
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			public DecimalFormat createInstance (Element elem) throws Exception
			{
				final String	fmt=elem.getAttribute(FORMAT_VIRTATTR);
				return new DecimalFormat(fmt);
			}
		};
}
