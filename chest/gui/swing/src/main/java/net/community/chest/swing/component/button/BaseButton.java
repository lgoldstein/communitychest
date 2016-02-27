/*
 * 
 */
package net.community.chest.swing.component.button;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenu;

import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.FontControl;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.awt.attributes.Tooltiped;
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
 * @since Aug 28, 2008 11:17:10 AM
 */
public class BaseButton extends JButton
		implements XmlConvertible<BaseButton>,
			       Textable, Iconable, FontControl, Tooltiped, Enabled,
			       Foregrounded, Backgrounded {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1128848867682295386L;
	public BaseButton ()
	{
		super();
	}

	public BaseButton (Icon icon)
	{
		super(icon);
	}

	public BaseButton (String text)
	{
		super(text);
	}

	public BaseButton (Action a)
	{
		super(a);
	}

	public BaseButton (String text, Icon icon)
	{
		super(text, icon);
	}

	protected XmlProxyConvertible<?> getButtonConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : JButtonReflectiveProxy.BUTTON;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseButton fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getButtonConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					o=
			((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + " mismatched initialization instances");

		return this;
	}

	public BaseButton (Element elem) throws Exception
	{
		if (fromXml(elem) != this)
			throw new IllegalStateException("<init>" + DOMUtils.toString(elem) + ") mismatched restored " + JMenu.class.getName() + " instances");
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
