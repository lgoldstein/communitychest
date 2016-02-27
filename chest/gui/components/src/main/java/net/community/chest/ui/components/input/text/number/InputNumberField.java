/*
 * 
 */
package net.community.chest.ui.components.input.text.number;

import java.text.NumberFormat;

import javax.swing.InputVerifier;

import org.w3c.dom.Element;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.ui.helpers.input.NumberInputFieldValidator;
import net.community.chest.ui.helpers.input.ValidatorUtils;
import net.community.chest.ui.helpers.text.InputTextField;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @param <N> The type of {@link Number} being input
 * @since Jan 12, 2009 1:42:33 PM
 */
public class InputNumberField<N extends Number & Comparable<N>> extends InputTextField
			implements TypedValuesContainer<N>,
					   TypedComponentAssignment<N>,
					   NumberInputFieldValidator<N> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2229187951362424431L;
	private final Class<N>	_numClass;
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final Class<N> getValuesClass ()
	{
		return _numClass;
	}

	private NumberFormat	_fmt;
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#getNumberFormat()
	 */
	@Override
	public NumberFormat getNumberFormat ()
	{
		return _fmt;
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#setNumberFormat(java.text.NumberFormat)
	 */
	@Override
	public void setNumberFormat (NumberFormat fmt)
	{
		if (_fmt != fmt)
			_fmt = fmt;
	}

	private N	_value;
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#getAssignedValue()
	 */
	@Override
	public N getAssignedValue ()
	{
		return _value;
	}

	public void setCurrentValue (N value, boolean updateUI)
	{
		if (_value != value)
		{
			if ((null == value) || (!value.equals(_value)))
				_value = value;
		}

		if (updateUI)
		{
			final NumberFormat	fmt=getNumberFormat();
			final String		txt;
			if (null == value)
				txt = null;
			else if (null == fmt)
				txt = value.toString();
			else
				txt = fmt.format(value); 
			setText((null == txt) ? "" : txt);
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#setAssignedValue(java.lang.Object)
	 */
	@Override
	public void setAssignedValue (N value)
	{
		setCurrentValue(value, true);
	}
	// NOTE !!! this does NOT change the assigned value
	public N getCurrentValue () throws RuntimeException
	{
		final String		text=getText();
		if ((null == text) || (text.length() <= 0))
			return null;

		final Class<N>		numClass=getValuesClass();
		final NumberFormat	fmt=getNumberFormat();
		try
		{
			if (null == fmt)
			{
				final ValueStringInstantiator<N>	vsi=ClassUtil.getAtomicStringInstantiator(numClass);
				return (null == vsi) ? null : vsi.newInstance(StringUtil.getCleanStringValue(text));
			}
			else
			{
				final Number	n=fmt.parse(text);
				if (null == n)
					return null;

				return numClass.cast(n);
			}
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}

	public InputNumberField (Class<N> numClass, Element elem, boolean autoLayout)
	{
		super(null, elem, false /* delay auto-layout until other preliminary initializations */);

		if (null == (_numClass=numClass))
			throw new IllegalArgumentException("No numbers class specified");

		if (autoLayout)
			layoutComponent();
	}

	@SuppressWarnings("unchecked")
	public InputNumberField (Element elem, boolean autoLayout)
	{
		this((null == elem) ? null : (Class<N>) InputNumberFieldReflectiveProxy.getNumberFieldClass(elem), elem, autoLayout);
	}

	public InputNumberField (Element elem)
	{
		this(elem, true);
	}

	public InputNumberField (Class<N> numClass, Element elem)
	{
		this(numClass, elem, true);
	}

	public InputNumberField (Class<N> numClass, boolean autoLayout)
	{
		this(numClass, null, autoLayout);
	}

	public InputNumberField (Class<N> numClass)
	{
		this(numClass, true);
	}


	// NOTE: throws IllegalArgumentException if null value provided
	@SuppressWarnings("unchecked")
	public InputNumberField (N iValue, boolean autoLayout)
	{
		this((null == iValue) ? null : (Class<N>) iValue.getClass(), autoLayout);
		setCurrentValue(iValue, true);
	}
	// NOTE: throws IllegalArgumentException if null value provided
	public InputNumberField (N iValue)
	{
		this(iValue, true);
	}

	private N	_minValue	/* =null */;
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#getMinValue()
	 */
	@Override
	public N getMinValue ()
	{
		return _minValue;
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#setMinValue(java.lang.Number)
	 */
	@Override
	public void setMinValue (N minValue)
	{
		_minValue = minValue;
	}

	private N	_maxValue	/* =null */;
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#getMaxValue()
	 */
	@Override
	public N getMaxValue ()
	{
		return _maxValue;
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#setMaxValue(java.lang.Number)
	 */
	@Override
	public void setMaxValue (N maxValue)
	{
		_maxValue = maxValue;
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#setAllowedValueRange(java.lang.Number, java.lang.Number)
	 */
	@Override
	public void setAllowedValueRange (N minValue, N maxValue)
	{
		// check if inverted range
		if ((minValue != null) && (maxValue != null) && (minValue.compareTo(maxValue) > 0))
		{
			setMinValue(maxValue);
			setMaxValue(minValue);
		}
		else
		{
			setMinValue(minValue);
			setMaxValue(maxValue);
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.text.InputTextField#createDefaultVerifier()
	 */
	@Override
	protected InputVerifier createDefaultVerifier ()
	{
		return ValidatorUtils.createNumberVerifier(getValuesClass(), getNumberFormat());
	}
	/*
	 * @see net.community.chest.ui.helpers.input.NumberInputFieldValidator#isValidNumber(java.lang.Number)
	 */
	@Override
	public boolean isValidNumber (N n)
	{
		if (null == n)
			return false;

		final N	minVal=getMinValue(), maxVal=getMaxValue();
		if ((minVal != null) && (minVal.compareTo(n) > 0))
			return false;

		if ((maxVal != null) && (maxVal.compareTo(n) < 0))
			return false;

		return true;
	}
	/*
	 * @see net.community.chest.ui.helpers.text.InputTextField#isValidData()
	 */
	@Override
	public boolean isValidData ()
	{
		if (!super.isValidData())
			return false;

		try
		{
			return isValidNumber(getCurrentValue());
		}
		catch(RuntimeException e)
		{
			return false;
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.text.InputTextField#signalDataChanged(boolean)
	 */
	@Override
	public int signalDataChanged (boolean fireEvent)
	{
		if (isValidData())
		{
			try
			{
				setCurrentValue(getCurrentValue(), false);
			}
			catch(RuntimeException e)
			{
				// ignored
			}
		}

		return super.signalDataChanged(fireEvent);
	}
}
