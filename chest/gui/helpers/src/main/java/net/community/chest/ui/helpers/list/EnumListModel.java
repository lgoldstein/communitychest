/*
 *
 */
package net.community.chest.ui.helpers.list;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>A {@link TypedListModel} that uses an {@link Enum} associated type</P>
 *
 * @param <E> The used {@link Enum} type
 * @author Lyor G.
 * @since Dec 4, 2008 12:59:31 PM
 */
public class EnumListModel<E extends Enum<E>> extends TypedListModel<E> {
    /**
     *
     */
    private static final long serialVersionUID = -2347233702693773584L;
    public EnumListModel (Class<E> valsClass) throws IllegalArgumentException
    {
        super(valsClass);
    }

    private E[]    _vals    /* =null */;
    public synchronized E[] getEnumValues ()
    {
        if (null == _vals)
            _vals = getValuesClass().getEnumConstants();
        return _vals;
    }

    public void setEnumValues (E[] vals)
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
}
