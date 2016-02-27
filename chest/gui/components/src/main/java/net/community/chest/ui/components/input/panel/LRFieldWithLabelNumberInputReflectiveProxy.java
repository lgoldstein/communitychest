/*
 * 
 */
package net.community.chest.ui.components.input.panel;

import javax.swing.JTextField;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.ui.components.input.text.number.InputNumberField;
import net.community.chest.ui.components.input.text.number.InputNumberFieldReflectiveProxy;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelInputReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <N> Type of {@link Number} being input 
 * @param <P> Type of {@link LRFieldWithLabelNumberInput} being reflected
 * @author Lyor G.
 * @since Jan 13, 2009 12:15:49 PM
 */
public class LRFieldWithLabelNumberInputReflectiveProxy<N extends Number & Comparable<N>, P extends LRFieldWithLabelNumberInput<N>>
		extends LRFieldWithLabelInputReflectiveProxy<P> {
	protected LRFieldWithLabelNumberInputReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public LRFieldWithLabelNumberInputReflectiveProxy (Class<P> objClass)
			throws IllegalArgumentException
	{
		this(objClass, false);
	}
	/*
	 * @see net.community.chest.ui.components.input.panel.LRFieldWithLabelInputReflectiveProxy#getTextFieldConverter(org.w3c.dom.Element)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public XmlProxyConvertible<? extends JTextField> getTextFieldConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : InputNumberFieldReflectiveProxy.INPNUMFLD;
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.input.InputTextPanelReflectiveProxy#setTextField(net.community.chest.ui.helpers.panel.input.AbstractInputTextPanel, org.w3c.dom.Element)
	 */
	@Override
	public JTextField setTextField (P src, Element elem) throws Exception
	{
		final Class<N>		numClass=src.getValuesClass();
		final JTextField	orgField=src.getTextField();
		if (null == orgField)
			src.setTextField(new InputNumberField<N>(numClass));

		return super.setTextField(src, elem);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final LRFieldWithLabelNumberInputReflectiveProxy	LRNUMINP=
		new LRFieldWithLabelNumberInputReflectiveProxy(InputNumberField.class, true) {
			/* Uses the "class" attribute to find out which field type is required
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXml(org.w3c.dom.Element)
			 */
			@Override
			public LRFieldWithLabelNumberInput fromXml (Element elem) throws Exception
			{
				final Class<?>	numClass=InputNumberFieldReflectiveProxy.getNumberFieldClass(elem);
				if (null == numClass)
					throw new ClassNotFoundException("fromXml(" + DOMUtils.toString(elem) + ") unknown class");

				return new LRFieldWithLabelNumberInput(numClass, elem, true /* auto-layout since have the element */);
			}
		};
}
