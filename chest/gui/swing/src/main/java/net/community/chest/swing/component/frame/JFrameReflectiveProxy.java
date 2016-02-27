package net.community.chest.swing.component.frame;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import net.community.chest.awt.frame.FrameReflectiveProxy;
import net.community.chest.awt.menu.MenuReflectiveProxy;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.WindowCloseOptionsValueStringInstantiator;
import net.community.chest.swing.component.menu.JMenuBarReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link JFrame} type 
 * @author Lyor G.
 * @since Mar 20, 2008 10:14:33 AM
 */
public class JFrameReflectiveProxy<F extends JFrame> extends FrameReflectiveProxy<F> {
	public JFrameReflectiveProxy (Class<F> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JFrameReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	DEFAULT_CLOSE_OPER_ATTR="DefaultCloseOperation";
	/*
	 * @see net.community.chest.awt.frame.FrameReflectiveProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if (DEFAULT_CLOSE_OPER_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) WindowCloseOptionsValueStringInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}

	public boolean isMenuBarElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, MenuReflectiveProxy.MENU_ELEMNAME);
	}

	public XmlProxyConvertible<? extends JMenuBar> getMenuBarConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : JMenuBarReflectiveProxy.BAR;
	}

	public JMenuBar setMenuBar (final F src, final Element elem) throws Exception
	{
		final XmlProxyConvertible<? extends JMenuBar>	inst=getMenuBarConverter(elem);
		@SuppressWarnings("unchecked")
		final JMenuBar									org=src.getJMenuBar(),
														bar=
			(null == org) ? inst.fromXml(elem) : ((XmlProxyConvertible<JMenuBar>) inst).fromXml(org, elem);
		if (bar != null)
		{
			if (null == org)
				src.setJMenuBar(bar);
			else if (org != bar)
				throw new IllegalStateException("setMenuBar(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
		}

		return bar;
	}
	/*
	 * @see net.community.chest.awt.dom.proxy.ContainerReflectiveProxy#fromXmlChild(java.awt.Container, org.w3c.dom.Element)
	 */
	@Override
	public F fromXmlChild (F src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isMenuBarElement(elem, tagName))
		{
			setMenuBar(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

	public static final JFrameReflectiveProxy<JFrame>	FRAME=
			new JFrameReflectiveProxy<JFrame>(JFrame.class, true);
}
