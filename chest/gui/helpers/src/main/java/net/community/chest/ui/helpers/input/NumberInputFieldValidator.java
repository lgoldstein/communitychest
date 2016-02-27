/*
 * 
 */
package net.community.chest.ui.helpers.input;

import java.text.NumberFormat;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <N> Type of {@link Number} being validated
 * @author Lyor G.
 * @since Jan 13, 2009 10:47:40 AM
 */
public interface NumberInputFieldValidator<N extends Number> extends InputFieldValidator {
	/**
	 * @return The {@link NumberFormat} to be used to display/parse/validate
	 * the input - if <code>null</code> then no special format set
	 */
	NumberFormat getNumberFormat ();
	void setNumberFormat (NumberFormat fmt);
	/**
	 * @return Minimum value allowed for the input data (inclusive) - if
	 * <code>null</code> then no specific limit imposed. <B>Note:</B> not
	 * validated if indeed below maximum value
	 * @see #getMaxValue()
	 */
	N getMinValue ();
	void setMinValue (N minValue);
	/**
	 * @return Maximum value allowed for the input data (inclusive) - if
	 * <code>null</code> then no specific limit imposed. <B>Note:</B> not
	 * validated if indeed above minimum value
	 * @see #getMinValue()
	 */
	N getMaxValue ();
	void setMaxValue (N maxValue);
	/**
	 * Allowed values range - any <code>null</code> range end-point means
	 * that no specific limit is imposed. Note: if range is <U>inverted</U>
	 * then the method automatically assumes that the low value is minimum
	 * and the high one maximum.
	 * @param minValue Minimum value (inclusive)
	 * @param maxValue Maximum value (inclusive)
	 * @see #setMinValue(Number)
	 * @see #setMaxValue(Number)
	 */
	void setAllowedValueRange (N minValue, N maxValue);
	/**
	 * @param n The {@link Number} to be checked
	 * @return <code>true</code> if value is within allowed range and
	 * "acceptable" by the validator
	 */
	boolean isValidNumber (N n);
}
