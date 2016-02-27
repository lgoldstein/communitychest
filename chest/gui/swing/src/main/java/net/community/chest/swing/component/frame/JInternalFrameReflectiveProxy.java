/*
 * 
 */
package net.community.chest.swing.component.frame;

import java.lang.reflect.Method;

import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;

import net.community.chest.awt.menu.MenuReflectiveProxy;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.WindowCloseOptionsValueStringInstantiator;
import net.community.chest.swing.component.JComponentReflectiveProxy;
import net.community.chest.swing.component.menu.JMenuBarReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link JInternalFrame} 
 * @author Lyor G.
 * @since Aug 27, 2008 3:51:58 PM
 */
public class JInternalFrameReflectiveProxy<F extends JInternalFrame> extends JComponentReflectiveProxy<F> {
	public JInternalFrameReflectiveProxy (Class<F> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JInternalFrameReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	DEFAULT_CLOSE_OPER_ATTR="DefaultCloseOperation",
								ICON_ATTR="frameIcon";
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if (DEFAULT_CLOSE_OPER_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) WindowCloseOptionsValueStringInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
	 */
	@Override
	protected F updateObjectAttribute (F src, String name, String value, Method setter) throws Exception
	{
		if (ICON_ATTR.equalsIgnoreCase(name))
			return updateObjectResourceAttribute(src, name, value, setter);

		return super.updateObjectAttribute(src, name, value, setter);
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
	 * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
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

	public static final JInternalFrameReflectiveProxy<JInternalFrame> INTFRAME=
			new JInternalFrameReflectiveProxy<JInternalFrame>(JInternalFrame.class, true);
}
