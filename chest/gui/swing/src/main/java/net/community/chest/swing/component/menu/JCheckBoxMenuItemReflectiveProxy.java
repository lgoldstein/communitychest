package net.community.chest.swing.component.menu;

import javax.swing.JCheckBoxMenuItem;

import net.community.chest.CoVariantReturn;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link JCheckBoxMenuItem} type 
 * @author Lyor G.
 * @since Mar 24, 2008 12:30:13 PM
 */
public class JCheckBoxMenuItemReflectiveProxy<B extends JCheckBoxMenuItem> extends JMenuItemReflectiveProxy<B> {
	public JCheckBoxMenuItemReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JCheckBoxMenuItemReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final JCheckBoxMenuItemReflectiveProxy<JCheckBoxMenuItem>	CBMENUITEM=
			new JCheckBoxMenuItemReflectiveProxy<JCheckBoxMenuItem>(JCheckBoxMenuItem.class, true) {
				/*
				 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
				 */
				@Override
				@CoVariantReturn
				public BaseCheckBoxMenuItem createInstance (Element elem) throws Exception
				{
					return (null == elem) ? null : new BaseCheckBoxMenuItem();
				}
		};
}
