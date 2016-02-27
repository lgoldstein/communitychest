package net.community.chest.swing.component.button;

import javax.swing.JButton;

import net.community.chest.CoVariantReturn;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link JButton} type
 * @author Lyor G.
 * @since Mar 20, 2008 8:42:55 AM
 */
public class JButtonReflectiveProxy<B extends JButton> extends AbstractButtonReflectiveProxy<B> {
	public JButtonReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public JButtonReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
			throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final JButtonReflectiveProxy<JButton>	BUTTON=
					new JButtonReflectiveProxy<JButton>(JButton.class, true) {
			/*
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			@CoVariantReturn
			public BaseButton createInstance (Element elem) throws Exception
			{
				return (null == elem) ? null : new BaseButton();
			}
		};
}
