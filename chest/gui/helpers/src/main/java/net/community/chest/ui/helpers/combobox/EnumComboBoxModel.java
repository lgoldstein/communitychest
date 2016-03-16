package net.community.chest.ui.helpers.combobox;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import net.community.chest.lang.EnumUtil;

import org.w3c.dom.Element;

/**
 * Copyright 2007 as per GPLv2
 *
 * Useful combo-box model when associated items are {@link Enum} values
 *
 * @param <E> The combo-box {@link Enum} generic type
 * @author Lyor G.
 * @since Jun 13, 2007 2:45:09 PM
 */
public class EnumComboBoxModel<E extends Enum<E>> extends TypedComboBoxModel<E> {
    /**
     *
     */
    private static final long serialVersionUID = 2248280680475226712L;
    public EnumComboBoxModel (Class<E> valsClass) throws IllegalArgumentException
    {
        super(valsClass);
    }

    private List<E>    _vals    /* =null */;
    public synchronized List<E> getEnumValues ()
    {
        if (null == _vals)
            _vals = Collections.unmodifiableList(Arrays.asList(getValuesClass().getEnumConstants()));
        return _vals;
    }

    public void setEnumValues (List<E> vals)
    {
        _vals = vals;
    }
    /**
     * Adds all enumeration values defined in the class
     * @see #getValuesClass()
     * @see Class#getEnumConstants()
     */
    public void addAllValues ()
    {
        addValues(getEnumValues());
    }
    /**
     * @return TRUE if translation from {@link String} to {@link Enum} value
     * should use case-sensitive search or not
     */
    public boolean isCaseSensitiveEnumName ()
    {
        return false;
    }
    /*
     * @see net.community.chest.swing.models.TypedComboBoxModel#fromXmlItemValueString(org.w3c.dom.Element, java.lang.String)
     */
    @Override
    public E fromXmlItemValueString (Element itemElem, String itemValStr) throws Exception
    {
        if ((null == itemValStr) || (itemValStr.length() <= 0))
            return null;    // OK if no associated value

        final E    value=EnumUtil.fromString(getValuesClass(), itemValStr, isCaseSensitiveEnumName());
        if (null == value)
            throw new NoSuchElementException("No matching " + getValuesClass().getName() + " value for name=" + itemValStr);

        return value;
    }
}
