/*
 * 
 */
package net.community.chest.ui.helpers.input;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <N> Type of {@link Number} being verified
 * @author Lyor G.
 * @since Jan 12, 2009 2:14:23 PM
 */
public class TextNumberInputVerifier<N extends Number>
			extends TextInputVerifier
			implements TypedValuesContainer<N> {
	private final Class<N>	_numClass;
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final Class<N> getValuesClass ()
	{
		return _numClass;
	}

	public TextNumberInputVerifier (final Class<N> numClass)
	{
		if (null == (_numClass=numClass))
			throw new IllegalArgumentException("No number class specified");
	}
	/*
	 * @see net.community.chest.ui.helpers.input.TextInputVerifier#verifyText(java.lang.String)
	 */
	@Override
	public boolean verifyText (final String text)
	{
		if (!super.verifyText(text))
			return false;

		final ValueStringInstantiator<N>	vsi=
			ClassUtil.getAtomicStringInstantiator(getValuesClass());
		try
		{
			final N	n=vsi.newInstance(text);
			return (n != null);
		}
		catch(Exception e)
		{
			return false;
		}
	}
}
