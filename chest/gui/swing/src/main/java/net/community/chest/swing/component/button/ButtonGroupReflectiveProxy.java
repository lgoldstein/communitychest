/*
 * 
 */
package net.community.chest.swing.component.button;

import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.dom.transform.XmlValueInstantiator;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <G> The reflected {@link ButtonGroup}
 * @author Lyor G.
 * @since Dec 14, 2008 12:25:55 PM
 */
public class ButtonGroupReflectiveProxy<G extends ButtonGroup> extends UIReflectiveAttributesProxy<G> {
	public ButtonGroupReflectiveProxy (Class<G> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected ButtonGroupReflectiveProxy (Class<G> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public boolean isButtonElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, AbstractButtonReflectiveProxy.BUTTON_ELEMNAME);
	}

	public boolean isRadioButtonGroupElement (final Element elem, final String itemClass)
	{
		return isMatchingAttribute(elem, itemClass, BaseRadioButton.RADIO_ELEMNAME);
	}

	public boolean isCheckboxGroupElement (final Element elem, final String itemClass)
	{
		return isMatchingAttribute(elem, itemClass, BaseCheckBox.CHECKBOX_ELEM_NAME);
	}

	public XmlValueInstantiator<? extends AbstractButton> getButtonConverter (final Element elem) throws Exception
	{
		final String	btnType=elem.getAttribute(CLASS_ATTR);
		if (isRadioButtonGroupElement(elem, btnType))
			return JRadioButtonReflectiveProxy.RADIO;
		else if (isCheckboxGroupElement(elem, btnType))
			return JCheckBoxReflectiveProxy.CB;
		else	// assume as default
			return JButtonReflectiveProxy.BUTTON;
	}

	public AbstractButton addButton (G src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends AbstractButton>	proxy=getButtonConverter(elem);
		final AbstractButton									b=(null == proxy) ? null : proxy.fromXml(elem);
		if (b != null)
			src.add(b);
		return b;
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public G fromXmlChild (G src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isButtonElement(elem, tagName))
		{
			addButton(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}
	/**
	 * Builds a {@link Map} of the buttons in the given {@link ButtonGroup}
	 * @param <B> The type of {@link AbstractButton} expected in the group
	 * @param bClass The exact {@link Class} of the expected button type
	 * @param g The {@link ButtonGroup} instance (may be null)
	 * @param org The {@link Map} to be updated - if non-null/empty then
	 * assumed to be up-to-date. If <code>null</code> then a case-<U>insensitive</U>
	 * map instance is allocated
	 * @param byCommand TRUE=use the {@link AbstractButton#getActionCommand()}
	 * value as mapping key, FALSE=use {{@link AbstractButton#getText()} value
	 * @return A {@link Map} where key=action command/text (depending on the
	 * <code>byCommand</code> parameter value), value=the matching button
	 * (cast to the <code>bClass</code> type). <B>Note:</B> null/empty values
	 * are <U>ignored</U> (i.e., not mapped).
	 * @throws IllegalStateException If more than one button mapped to the same key
	 */
	public static final <B extends AbstractButton> Map<String,B> updateButtonsMap (
			final Class<B>		bClass,
			final ButtonGroup	g,
			final Map<String,B> org,
			final boolean		byCommand) throws IllegalStateException
	{
		if ((org != null) && (org.size() > 0))
			return org;	// assume initialized

		Map<String,B>	ret=org;
		for (final Enumeration<? extends AbstractButton>	eb=(null == g) ? null : g.getElements();
			 (eb != null) && eb.hasMoreElements();
			 )
		{
			final AbstractButton	ab=eb.nextElement();
			if (null == ab)
				continue;

			final String	key=byCommand ? ab.getActionCommand() : ab.getText();
			if ((null == key) || (key.length() <= 0))
				continue;

			if (null == ret)
				ret = new TreeMap<String,B>(String.CASE_INSENSITIVE_ORDER);

			final B	prev=ret.put(key, bClass.cast(ab));
			if (prev != null)
				throw new IllegalStateException("updateButtonsMap(cmd=" + byCommand + ") multiple values for key=" + key);
		}

		return ret;
	}

	public static final <B extends AbstractButton> Map<String,B> getButtonsMap (
			final Class<B>		bClass,
			final ButtonGroup	g,
			final boolean		byCommand) throws IllegalStateException
	{
		return updateButtonsMap(bClass, g, null, byCommand);
	}

	public static final Map<String,AbstractButton> updateButtonsMap (
			final Map<String,AbstractButton>	org,
			final ButtonGroup					g,
			final boolean						byCommand) throws IllegalStateException
	{
		return updateButtonsMap(AbstractButton.class, g, org, byCommand);
	}

	public static final Map<String,AbstractButton> getButtonsMap (
					final ButtonGroup	g,
					final boolean		byCommand) throws IllegalStateException
	{
		return updateButtonsMap(null, g, byCommand);
	}

	public static final ButtonGroupReflectiveProxy<ButtonGroup>	BG=
		new ButtonGroupReflectiveProxy<ButtonGroup>(ButtonGroup.class, true);
}
