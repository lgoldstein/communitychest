/*
 * 
 */
package net.community.chest.ui.components.input.text.number;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 12, 2009 3:08:44 PM
 */
public class FloatInputNumberField extends InputNumberField<Float> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2236094121482664174L;
	public FloatInputNumberField (Element elem, boolean autoLayout)
	{
		super(Float.class, elem, autoLayout);
	}

	public FloatInputNumberField (boolean autoLayout)
	{
		this(null, autoLayout);
	}

	public FloatInputNumberField ()
	{
		this(true);
	}
	/* Disallows also NaN and INFINITY
	 * @see net.community.chest.ui.components.input.text.InputNumberField#isValidNumber(java.lang.Number)
	 */
	@Override
	public boolean isValidNumber (Float n)
	{
		if (null == n)
			return false;

		final float	f=n.floatValue();
		if (Float.isNaN(f) || Float.isInfinite(f))
			return false;

		return super.isValidNumber(n);
	}
}
