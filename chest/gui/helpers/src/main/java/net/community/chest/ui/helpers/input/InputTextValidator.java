package net.community.chest.ui.helpers.input;

import net.community.chest.convert.ValueStringInstantiator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 * 
 * <P>Used to validate textual data in input fields</P>
 *
 * @param <V> The validated type
 * @author Lyor G.
 * @since May 21, 2008 3:42:51 PM
 */
public interface InputTextValidator<V> extends ValueStringInstantiator<V> {
	/**
	 * @param text Input data {@link String}
	 * @return TRUE if the text representation of the value is valid
	 */
	boolean isValidText (String text);
	/**
	 * @param value The {@link Object} to be set as the text value
	 * @return TRUE if the object is valid for the text field/value
	 */
	boolean isValidObject (V value);
}
