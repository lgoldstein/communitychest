/*
 * 
 */
package net.community.chest.ui.components.input.panel;

import java.text.NumberFormat;

import javax.swing.JTextField;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.ui.components.input.text.number.InputNumberField;
import net.community.chest.ui.components.input.text.number.InputNumberFieldReflectiveProxy;
import net.community.chest.ui.helpers.input.NumberInputFieldValidator;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelInput;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <N> The type of {@link Number} being input
 * @author Lyor G.
 * @since Jan 13, 2009 11:31:21 AM
 */
public class LRFieldWithLabelNumberInput<N extends Number & Comparable<N>> extends LRFieldWithLabelInput
		implements TypedValuesContainer<N>,
		   		   TypedComponentAssignment<N>,
		   		   NumberInputFieldValidator<N> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1322210280623920341L;
	private final Class<N>	_numClass;
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final Class<N> getValuesClass ()
	{
		return _numClass;
	}
	/*
	 * @see net.community.chest.ui.components.input.panel.LRFieldWithLabelInput#getTextField(boolean)
	 */
	@Override
	protected JTextField getTextField (boolean createIfNotExist)
	{
		JTextField	f=super.getTextField(false);
		if ((null == f) && createIfNotExist)
		{
			f = new InputNumberField<N>(getValuesClass());
			setTextField(f);
		}

		return f;
	}
	/*
	 * @see net.community.chest.ui.components.input.panel.LRFieldWithLabelInput#getPanelConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPanelConverter (Element elem)
	{
		return (null == elem) ? null : LRFieldWithLabelNumberInputReflectiveProxy.LRNUMINP;
	}

	public LRFieldWithLabelNumberInput (Class<N> numClass, Document doc, boolean autoLayout)
	{
		super(doc, false /* delay auto-layout till number class initialized */);

		if (null == (_numClass=numClass))
			throw new IllegalArgumentException("No numbers class specified");

		if (autoLayout)
			layoutComponent();
	}

	@SuppressWarnings("unchecked")
	public LRFieldWithLabelNumberInput (Document doc, boolean autoLayout)
	{
		this((null == doc) ? null : (Class<N>) InputNumberFieldReflectiveProxy.getNumberFieldClass(doc.getDocumentElement()), doc, autoLayout); 
	}

	public LRFieldWithLabelNumberInput (Document doc)
	{
		this(doc, true);
	}

	public LRFieldWithLabelNumberInput (Class<N> numClass, Document doc)
	{
		this(numClass, doc, true);
	}

	public LRFieldWithLabelNumberInput (Class<N> numClass, boolean autoLayout)
	{
		this(numClass, (Document) null, autoLayout);
	}

	public LRFieldWithLabelNumberInput (Class<N> numClass)
	{
		this(numClass, true);
	}

	public LRFieldWithLabelNumberInput (Element elem, boolean autoLayout)
	{
		this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public LRFieldWithLabelNumberInput (Element elem)
	{
		this(elem, true);
	}

	public LRFieldWithLabelNumberInput (Class<N> numClass, Element elem, boolean autoLayout)
	{
		this(numClass, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public LRFieldWithLabelNumberInput (Class<N> numClass, Element elem)
	{
		this(numClass, elem, true);
	}

	@SuppressWarnings("unchecked")
	public InputNumberField<N> getInputNumberField ()
	{
		final JTextField	f=getTextField();
		return (f instanceof InputNumberField) ? (InputNumberField<N>) f : null;
	}

	public void setInputNumberField (InputNumberField<N> f)
	{
		setTextField(f);
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#getMaxValue()
	 */
	@Override
	public N getMaxValue ()
	{
		final InputNumberField<N>	f=getInputNumberField();
		return (null == f) ? null : f.getMaxValue();
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#getMinValue()
	 */
	@Override
	public N getMinValue ()
	{
		final InputNumberField<N>	f=getInputNumberField();
		return (null == f) ? null : f.getMinValue();
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#getNumberFormat()
	 */
	@Override
	public NumberFormat getNumberFormat ()
	{
		final InputNumberField<N>	f=getInputNumberField();
		return (null == f) ? null : f.getNumberFormat();
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#isValidNumber(java.lang.Number)
	 */
	@Override
	public boolean isValidNumber (N n)
	{
		final InputNumberField<N>	f=getInputNumberField();
		return (null == f) ? false : f.isValidNumber(n);
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#setAllowedValueRange(java.lang.Number, java.lang.Number)
	 */
	@Override
	public void setAllowedValueRange (N minValue, N maxValue)
	{
		final InputNumberField<N>	f=getInputNumberField();
		if (f != null)
			f.setAllowedValueRange(minValue, maxValue);
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#setMaxValue(java.lang.Number)
	 */
	@Override
	public void setMaxValue (N maxValue)
	{
		final InputNumberField<N>	f=getInputNumberField();
		if (f != null)
			f.setMaxValue(maxValue);
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#setMinValue(java.lang.Number)
	 */
	@Override
	public void setMinValue (N minValue)
	{
		final InputNumberField<N>	f=getInputNumberField();
		if (f != null)
			f.setMinValue(minValue);
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#setNumberFormat(java.text.NumberFormat)
	 */
	@Override
	public void setNumberFormat (NumberFormat fmt)
	{
		final InputNumberField<N>	f=getInputNumberField();
		if (f != null)
			f.setNumberFormat(fmt);
	}
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#getAssignedValue()
	 */
	@Override
	public N getAssignedValue ()
	{
		final InputNumberField<N>	f=getInputNumberField();
		return (null == f) ? null : f.getAssignedValue();
	}
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#setAssignedValue(java.lang.Object)
	 */
	@Override
	public void setAssignedValue (N value)
	{
		final InputNumberField<N>	f=getInputNumberField();
		if (f != null)
			f.setAssignedValue(value);
	}
}
