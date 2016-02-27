/*
 * 
 */
package net.community.chest.swing.component.button;

import javax.swing.JRadioButton;

import net.community.chest.CoVariantReturn;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link JRadioButton} instance 
 * @author Lyor G.
 * @since Aug 28, 2008 11:25:17 AM
 */
public class JRadioButtonReflectiveProxy<B extends JRadioButton> extends JToggleButtonReflectiveProxy<B> {
	public JRadioButtonReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JRadioButtonReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final JRadioButtonReflectiveProxy<JRadioButton>	RADIO=
			new JRadioButtonReflectiveProxy<JRadioButton>(JRadioButton.class, true) {
			/*
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			@CoVariantReturn
			public BaseRadioButton createInstance (Element elem) throws Exception
			{
				return (null == elem) ? null : new BaseRadioButton();
			}
		};
}
