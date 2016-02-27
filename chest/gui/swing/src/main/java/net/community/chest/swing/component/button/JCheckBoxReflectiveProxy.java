/*
 * 
 */
package net.community.chest.swing.component.button;

import javax.swing.JCheckBox;

import net.community.chest.CoVariantReturn;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link JCheckBox} instance 
 * @author Lyor G.
 * @since Aug 21, 2008 3:44:23 PM
 */
public class JCheckBoxReflectiveProxy<B extends JCheckBox> extends JToggleButtonReflectiveProxy<B> {
	public JCheckBoxReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JCheckBoxReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final JCheckBoxReflectiveProxy<JCheckBox>	CB=
			new JCheckBoxReflectiveProxy<JCheckBox>(JCheckBox.class, true) {
		/* Improved API
		 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
		 */
		@Override
		@CoVariantReturn
		public BaseCheckBox createInstance (Element elem) throws Exception
		{
			return (null == elem) ? null : new BaseCheckBox();
		}
	};
}
