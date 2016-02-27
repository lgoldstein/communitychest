/*
 * 
 */
package net.community.chest.awt.systray;

import java.awt.SystemTray;
import java.awt.TrayIcon;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.dom.transform.XmlValueInstantiator;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <T> The reflected {@link SystemTray} instance
 * @author Lyor G.
 * @since Sep 7, 2008 4:05:41 PM
 */
public class SystemTrayReflectiveProxy<T extends SystemTray> extends UIReflectiveAttributesProxy<T> {
	public SystemTrayReflectiveProxy (Class<T> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected SystemTrayReflectiveProxy (Class<T> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	ICON_ELEMNAME="icon";
	public boolean isTrayIconElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, ICON_ELEMNAME);
	}

	public XmlValueInstantiator<? extends TrayIcon> getTrayIconConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : TrayIconReflectiveProxy.TRAYICON;
	}

	public TrayIcon createTrayIcon (final T src, final Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends TrayIcon>	proxy=getTrayIconConverter(elem);
		final TrayIcon									icon=proxy.fromXml(elem);
		if (icon != null)
			src.add(icon);

		return icon;
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public T fromXmlChild (final T src, final Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isTrayIconElement(elem, tagName))
		{
			createTrayIcon(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

	public static final SystemTrayReflectiveProxy<SystemTray> SYSTRAY=
			new SystemTrayReflectiveProxy<SystemTray>(SystemTray.class, true) {
				/*
				 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
				 */
				@Override
				public SystemTray createInstance (Element elem) throws Exception
				{
					return SystemTray.getSystemTray();
				}
			};
}
