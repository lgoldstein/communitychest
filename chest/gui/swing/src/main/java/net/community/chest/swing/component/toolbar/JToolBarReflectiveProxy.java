/*
 * 
 */
package net.community.chest.swing.component.toolbar;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.JToolBar;

import net.community.chest.awt.dom.converter.DimensionValueInstantiator;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.swing.ActionReflectiveProxy;
import net.community.chest.swing.SwingConstantsValueStringInstantiator;
import net.community.chest.swing.component.JComponentReflectiveProxy;
import net.community.chest.swing.component.JSeparatorReflectiveProxy;
import net.community.chest.swing.component.button.AbstractButtonReflectiveProxy;
import net.community.chest.swing.component.button.BaseCheckBox;
import net.community.chest.swing.component.button.BaseRadioButton;
import net.community.chest.swing.component.button.JButtonReflectiveProxy;
import net.community.chest.swing.component.button.JCheckBoxReflectiveProxy;
import net.community.chest.swing.component.button.JRadioButtonReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link JToolBar}
 * @author Lyor G.
 * @since Sep 24, 2008 10:56:13 AM
 */
public class JToolBarReflectiveProxy<B extends JToolBar> extends JComponentReflectiveProxy<B> {
	public JToolBarReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JToolBarReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
	// special attribute
	public static final String	ORIENTATION_ATTR="orientation";
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if (ORIENTATION_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) SwingConstantsValueStringInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}

	// XML sub-elements names
	public boolean isSeparatorElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, JSeparatorReflectiveProxy.SEPARATOR_ELEMNAME);
	}

	public B createToolBarSeparator (final B src, final Element elem) throws Exception
	{
		final String	sz=elem.getAttribute(SIZE_ATTR);
		if ((sz != null) && (sz.length() > 0))
		{
			final Dimension	d=DimensionValueInstantiator.DEFAULT.newInstance(sz);
			src.addSeparator(d);
		}
		else
			src.addSeparator();

		return src;
	}

	public boolean isRadioButtonToolbarElement (final Element elem, final String itemClass)
	{
		return isMatchingAttribute(elem, itemClass, BaseRadioButton.RADIO_ELEMNAME);
	}

	public boolean isCheckboxToolbarElement (final Element elem, final String itemClass)
	{
		return isMatchingAttribute(elem, itemClass, BaseCheckBox.CHECKBOX_ELEM_NAME);
	}

	public boolean isButtonElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, AbstractButtonReflectiveProxy.BUTTON_ELEMNAME);
	}
	// default check the "class" attribute for "radio" or "checkbox" - default=JButton
	public XmlValueInstantiator<? extends Component> getButtonConverter (final Element elem) throws Exception
	{
		final String	btnType=elem.getAttribute(CLASS_ATTR);
		if (isRadioButtonToolbarElement(elem, btnType))
			return JRadioButtonReflectiveProxy.RADIO;
		else if (isCheckboxToolbarElement(elem, btnType))
			return JCheckBoxReflectiveProxy.CB;
		else	// assume as default
			return JButtonReflectiveProxy.BUTTON;
	}

	public Component addButton (final B src, final Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends Component>	inst=getButtonConverter(elem);
		final Component									c=inst.fromXml(elem);
		if (c != null)
			src.add(c);

		return c;
	}

	public boolean isActionElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, ActionReflectiveProxy.ACTION_ELEMNAME);
	}
	// throws UnsupportedOperationException by default
	public XmlValueInstantiator<? extends Action> getActionConverter (final Element elem) throws Exception
	{
		if (null == elem)
			return null;

		throw new UnsupportedOperationException("getActionConverter(" + DOMUtils.toString(elem) + ") N/A");
	}

	public Action addAction (final B src, final Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends Action>	inst=getActionConverter(elem);
		final Action									a=inst.fromXml(elem);
		if (a != null)
			src.add(a);

		return a;
	}
	/*
	 * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
	 */
	@Override
	public B fromXmlChild (B src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isButtonElement(elem, tagName))
		{
			addButton(src, elem);
			return src;
		}
		else if (isActionElement(elem, tagName))
		{
			addAction(src, elem);
			return src;
		}
		else if (isSeparatorElement(elem, tagName))
			return createToolBarSeparator(src, elem);

		return super.fromXmlChild(src, elem);
	}

	public static final JToolBarReflectiveProxy<JToolBar>	TOOLBAR=
			new JToolBarReflectiveProxy<JToolBar>(JToolBar.class, true);
}
