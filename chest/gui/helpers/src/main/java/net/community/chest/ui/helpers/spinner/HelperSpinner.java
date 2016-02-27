/*
 * 
 */
package net.community.chest.ui.helpers.spinner;

import javax.swing.SpinnerModel;

import org.w3c.dom.Element;

import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.spinner.BaseSpinner;
import net.community.chest.swing.component.spinner.BaseSpinnerNumberModel;
import net.community.chest.ui.helpers.XmlElementComponentInitializer;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 12, 2009 8:11:34 AM
 *
 */
public class HelperSpinner extends BaseSpinner implements XmlElementComponentInitializer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8788550974300150468L;
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

	public HelperSpinner (SpinnerModel model, Element elem, boolean autoLayout)
	{
		super(model);

		setComponentElement(elem);
		if (autoLayout)
			layoutComponent();
	}

	public HelperSpinner (SpinnerModel model, boolean autoLayout)
	{
		this(model, null, autoLayout);
	}

	public HelperSpinner (SpinnerModel model)
	{
		this(model, true);
	}

	public HelperSpinner (SpinnerModel model, Element elem)
	{
		this(model, elem, true);
	}

	public HelperSpinner (Element elem, boolean autoLayout)
	{
		this(new BaseSpinnerNumberModel(), elem, autoLayout);
	}

	public HelperSpinner (Element elem)
	{
		this(elem, true);
	}

	public HelperSpinner (boolean autoLayout)
	{
		this((Element) null, autoLayout);
	}

	public HelperSpinner ()
	{
		this(true);
	}
}
