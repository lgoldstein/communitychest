package net.community.chest.swing.component.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.menu.MenuReflectiveProxy;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.swing.component.JComponentReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The {@link JMenuBar} class being reflected
 * @author Lyor G.
 * @since Mar 20, 2008 9:19:51 AM
 */
public class JMenuBarReflectiveProxy<B extends JMenuBar> extends JComponentReflectiveProxy<B> {
	public JMenuBarReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JMenuBarReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public XmlValueInstantiator<? extends JMenu> getMenuConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : JMenuReflectiveProxy.MENU;
	}
	/**
	 * Called by {@link #fromXmlChild(JMenuBar, Element)} when a sub-menu
	 * XML {@link Element} is encountered in order to re-construct the
	 * sub-menu
	 * @param src The {@link JMenu} instance to which to add the sub-menu
	 * @param elem XML element to use for data reconstruction
	 * @return Created {@link JMenu} instance - default calls
	 * {@link #getMenuConverter(Element)} and then invokes its
	 * {@link XmlValueInstantiator#fromXml(Element)} method
	 * @throws Exception if cannot re-construct the sub-menu
	 */
	public JMenu createSubMenu (final B src, final Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends JMenu>	proxy=getMenuConverter(elem);
		final JMenu									subMenu=proxy.fromXml(elem);
		if (subMenu != null)
			src.add(subMenu);

		return subMenu;
	}

	public boolean isSubMenuElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, MenuReflectiveProxy.MENU_ELEMNAME);
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public B fromXmlChild (B src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isSubMenuElement(elem, tagName))
		{
			createSubMenu(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

	public static final JMenuBarReflectiveProxy<JMenuBar>	BAR=
			new JMenuBarReflectiveProxy<JMenuBar>(JMenuBar.class, true) {
				/* Since it implements some very useful interfaces
				 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXml(org.w3c.dom.Element)
				 */
				@Override
				@CoVariantReturn
				public BaseMenuBar createInstance (Element elem) throws Exception
				{
					return (null == elem) ? null : new BaseMenuBar();
				}
		};
}
