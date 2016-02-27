/*
 * 
 */
package net.community.chest.ui.helpers.button;

import org.w3c.dom.Element;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.swing.component.button.JCheckBoxReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> Type of value contained in the checkbox
 * @param <B> Type of {@link TypedCheckBox} being reflected
 * @author Lyor G.
 * @since Jan 13, 2009 2:54:29 PM
 */
public class TypedCheckBoxReflectiveProxy<V,B extends TypedCheckBox<V>> extends JCheckBoxReflectiveProxy<B> {
	protected TypedCheckBoxReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public TypedCheckBoxReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final TypedCheckBoxReflectiveProxy	TYPCB=
		new TypedCheckBoxReflectiveProxy(TypedCheckBox.class, true) {
			/*
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXml(org.w3c.dom.Element)
			 */
			@Override
			@CoVariantReturn
			public TypedCheckBox fromXml (Element elem) throws Exception
			{
				final Class<?>	vc=loadElementClass(elem);
				if (null == vc)
					throw new ClassNotFoundException("fromXml(" + DOMUtils.toString(elem) + ") no class found");
	
				return new TypedCheckBox(vc, elem, true /* auto-layout */);
			}
	};
}
