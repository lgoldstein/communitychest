/*
 *
 */
package net.community.chest.ui.helpers.spinner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <E> The {@link Enum} value type
 * @author Lyor G.
 * @since Dec 16, 2008 2:22:35 PM
 */
public class EnumSpinnerListModel<E extends Enum<E>> extends TypedSpinnerListModel<E> {
    /**
     *
     */
    private static final long serialVersionUID = -6446694018042928394L;
    public EnumSpinnerListModel (Class<E> valsClass, boolean cyclic) throws IllegalArgumentException
    {
        super(valsClass, cyclic);
    }

    public EnumSpinnerListModel (Class<E> valsClass) throws IllegalArgumentException
    {
        this(valsClass, false);
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

}
