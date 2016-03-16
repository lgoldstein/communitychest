/*
 *
 */
package net.community.chest.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.convert.ValueStringInstantiator;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>An interface used to represent some holder of simple parameters values</P>
 * @author Lyor G.
 * @since Nov 24, 2010 8:49:07 AM
 */
public interface ParametersHolder {
    /**
     * @param paramName Parameter name
     * @param defValue Value to return if parameter not found
     * @return Parameter value - default value if parameter not found
     */
    String getParameter (String paramName, String defValue);
    /**
     * @param paramName Parameter name
     * @return Parameter value - <code>null</code> if parameter not found
     */
    String getParameter (String paramName);
    /**
     * The {@link List} of values considered <code>true</code>
     */
    public static final List<String>    TRUE_PARAM_VALUES=
            Collections.unmodifiableList(Arrays.asList("true", "1", "yes", "y"));
    /**
     * Extracts a boolean flag value parameter from a request
     * @param paramName Parameter name from which to extract the value
     * @return TRUE if extracted string is <code>true</code>, <code>1</code>, <code>yes</code>
     * or <code>y</code> (case insensitive). If parameter had no value then returns <code>null</code>
     * @see #TRUE_PARAM_VALUES
     */
    Boolean getFlagParameter (String paramName);
    /**
     * Extracts a boolean flag value parameter from a request - if not found,
     * then returns the specified default value
     * @param paramName Parameter name from which to extract the value
     * @param defValue Default value to return if parameter not found
     * @return Extracted value or default
     * @see #getFlagParameter(String) auxiliary function
     */
    boolean getFlagParameter (String paramName, boolean defValue);
    /**
     * Extracts an {@link Integer} value from a request
     * @param paramName Parameter name from which to extract the value
     * @return Parsed value or <code>null</code> if no value extracted
     * @throws NumberFormatException If bad extracted value format/value
     */
    Integer getIntParameter (String paramName) throws NumberFormatException;
    /**
     * Extracts an <code>int</code> value from a request
     * @param paramName Parameter name from which to extract the value
     * @return Parsed value or <code>null</code> if no value extracted
     * @param defValue Default value to return if parameter not found
     * @throws NumberFormatException If bad extracted value format/value
     * @see #getIntParameter(String)
     */
    int getIntParameter (String paramName, int defValue) throws NumberFormatException;
    /**
     * Extracts an {@link Long} value from a request
     * @param paramName Parameter name from which to extract the value
     * @return Parsed value or <code>null</code> if no value extracted
     * @throws NumberFormatException If bad extracted value format/value
     */
    Long getLongParameter (String paramName) throws NumberFormatException;
    /**
     * Extracts a <code>long</code> value from a request
     * @param paramName Parameter name from which to extract the value
     * @return Parsed value or <code>null</code> if no value extracted
     * @param defValue Default value to return if parameter not found
     * @throws NumberFormatException If bad extracted value format/value
     * @see #getLongParameter(String)
     */
    long getLongParameter (String paramName, long defValue) throws NumberFormatException;
    /**
     * @param <V> Type of value being retrieved
     * @param paramName Parameter name from which to extract the value
     * @param vsi The {@link ValueStringInstantiator} to use for converting the extracted
     * string (if any) to the actual object
     * @return Instantiated object - <code>null</code> if parameter does not exist
     * @throws Exception If failed to instantiate the value
     * @see ValueStringInstantiator#newInstance(String)
     */
    <V> V getParameterValue (String paramName, ValueStringInstantiator<? extends V> vsi)
        throws Exception;
    /**
     * @param <V> Type of value being retrieved
     * @param paramName Parameter name from which to extract the value
     * @param vsi The {@link ValueStringInstantiator} to use for converting the extracted
     * string (if any) to the actual object
     * @param defValue Default value to return if parameter not found
     * @return Instantiated object - default value if parameter does not exist
     * @throws Exception If failed to instantiate the value
     * @see #getParameterValue(String, ValueStringInstantiator)
     */
    <V> V getParameterValue (String paramName, ValueStringInstantiator<? extends V> vsi, V defValue)
        throws Exception;
}
