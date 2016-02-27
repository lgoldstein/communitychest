package net.community.chest.swing.component.menu;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 12:27:44 PM
 */
public class BaseCheckBoxMenuItem extends JCheckBoxMenuItem implements XmlConvertible<BaseCheckBoxMenuItem> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6143889921257322841L;
	public BaseCheckBoxMenuItem ()
	{
		super();
	}

	public BaseCheckBoxMenuItem (Icon icon)
	{
		super(icon);
	}

	public BaseCheckBoxMenuItem (String text)
	{
		super(text);
	}

	public BaseCheckBoxMenuItem (Action a)
	{
		super(a);
	}

	public BaseCheckBoxMenuItem (String text, Icon icon)
	{
		super(text, icon);
	}

	public BaseCheckBoxMenuItem (String text, boolean b)
	{
		super(text, b);
	}

	public BaseCheckBoxMenuItem (String text, Icon icon, boolean b)
	{
		super(text, icon, b);
	}

	public XmlProxyConvertible<?> getCheckBoxMenuItemConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : JCheckBoxMenuItemReflectiveProxy.CBMENUITEM;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseCheckBoxMenuItem fromXml (final Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getCheckBoxMenuItemConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					o=
			((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "fromXml", DOMUtils.toString(elem)) + " mismatched re-constructed instances");

		return this;
	}

	public BaseCheckBoxMenuItem  (Element elem) throws Exception
	{
		final BaseCheckBoxMenuItem	item=fromXml(elem);
		if (item != this)	// not allowed
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched restored " + BaseMenuItem.class.getName() + " instances");
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		// TODO implement toXml
		throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
	}
}
