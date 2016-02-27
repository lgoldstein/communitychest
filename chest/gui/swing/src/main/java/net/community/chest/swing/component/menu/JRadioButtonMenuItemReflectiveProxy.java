package net.community.chest.swing.component.menu;

import javax.swing.JRadioButtonMenuItem;

import net.community.chest.CoVariantReturn;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link JRadioButtonMenuItem} type 
 * @author Lyor G.
 * @since Mar 24, 2008 12:25:33 PM
 */
public class JRadioButtonMenuItemReflectiveProxy<B extends JRadioButtonMenuItem> extends JMenuItemReflectiveProxy<B> {
	public JRadioButtonMenuItemReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JRadioButtonMenuItemReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final JRadioButtonMenuItemReflectiveProxy<JRadioButtonMenuItem>	RADIOMENUITEM=
			new JRadioButtonMenuItemReflectiveProxy<JRadioButtonMenuItem>(JRadioButtonMenuItem.class, true) {
				/*
				 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
				 */
				@Override
				@CoVariantReturn
				public BaseRadioButtonMenuItem createInstance (Element elem) throws Exception
				{
					return (null == elem) ? null : new BaseRadioButtonMenuItem();
				}
		};
}
