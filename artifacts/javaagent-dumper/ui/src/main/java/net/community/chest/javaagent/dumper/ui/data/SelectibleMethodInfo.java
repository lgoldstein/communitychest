/*
 * 
 */
package net.community.chest.javaagent.dumper.ui.data;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.community.chest.awt.attributes.Selectible;
import net.community.chest.javaagent.dumper.data.MethodInfo;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 11:40:45 AM
 */
public class SelectibleMethodInfo extends MethodInfo implements Selectible {
	private static final long serialVersionUID = 5098454677877029988L;

	public SelectibleMethodInfo ()
	{
		super();
	}

	public SelectibleMethodInfo (Element root) throws Exception
	{
		super(root);
	}

	private boolean	_selected;
	/*
	 * @see net.community.chest.awt.attributes.Selectible#isSelected()
	 */
	@Override
	public boolean isSelected ()
	{
		return _selected;
	}
	/*
	 * @see net.community.chest.awt.attributes.Selectible#setSelected(boolean)
	 */
	@Override
	public void setSelected (boolean v)
	{
		_selected = v;
	}

	protected Boolean setSelected (Element elem)
	{
		final String	value=elem.getAttribute(ATTR_NAME);
		if ((value == null) || (value.length() <= 0))
			return null;

		final Boolean	selected=Boolean.valueOf(value);
		setSelected(selected.booleanValue());
		return selected;
	}
	/*
	 * @see net.community.chest.javaagent.dumper.data.MethodInfo#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		final Element	elem=super.toXml(doc);
		elem.setAttribute(ATTR_NAME, String.valueOf(isSelected()));
		return elem;
	}
	/*
	 * @see net.community.chest.javaagent.dumper.data.MethodInfo#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public SelectibleMethodInfo fromXml (Element root) throws Exception
	{
		final Object	info=super.fromXml(root);
		if (info != this)
			throw new IllegalStateException("Mismatched super-class re-constructed instance");

		setSelected(root);
		return this;
	}
}
