/*
 * 
 */
package net.community.chest.ui.helpers.button;

import javax.swing.Icon;

import org.w3c.dom.Element;

import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.button.BaseCheckBox;
import net.community.chest.ui.helpers.XmlElementComponentInitializer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 13, 2009 2:25:52 PM
 */
public class HelperCheckBox extends BaseCheckBox implements XmlElementComponentInitializer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7531308848140631234L;
	private	Element	_cElem	/* =null */;
	/*
	 * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#getComponentElement()
	 */
	@Override
	public Element getComponentElement () throws RuntimeException
	{
		return _cElem;
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#setComponentElement(org.w3c.dom.Element)
	 */
	@Override
	public void setComponentElement (Element elem)
	{
		if (_cElem != elem)
			_cElem = elem;
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#layoutComponent(org.w3c.dom.Element)
	 */
	@Override
	public void layoutComponent (Element elem) throws RuntimeException
	{
		if (elem != null)
		{
			try
			{
				if (fromXml(elem) != this)
					throw new IllegalStateException("layoutComponent(" + DOMUtils.toString(elem) + ") mismatched re-constructed instance");
			}
			catch(Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}

			setComponentElement(elem);
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.ComponentInitializer#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		layoutComponent(getComponentElement());
	}

	public HelperCheckBox (String text, Icon icon, boolean selected, Element elem, boolean autoLayout)
	{
		super(text, icon, selected);

		setComponentElement(elem);
		if (autoLayout)
			layoutComponent();
	}

	public HelperCheckBox (String text, Icon icon, boolean selected)
	{
		this(text, icon, selected, null, true);
	}

	public HelperCheckBox (Icon icon, boolean selected)
	{
		this(null, icon, selected);
	}

	public HelperCheckBox (Element elem, boolean autoLayout)
	{
		this(null, null, false, elem, autoLayout);
	}

	public HelperCheckBox (Element elem)
	{
		this(elem, true);
	}

	public HelperCheckBox (boolean autoLayout)
	{
		this((Element) null, autoLayout);
	}

	public HelperCheckBox ()
	{
		this(true);
	}

	public HelperCheckBox (Icon icon)
	{
		this(icon, false);
	}

	public HelperCheckBox (String text, boolean selected)
	{
		this(text, null, selected);
	}


	public HelperCheckBox (String text, Icon icon)
	{
		this(text, icon, false);
	}

	public HelperCheckBox (String text)
	{
		this(text, false);
	}
}
