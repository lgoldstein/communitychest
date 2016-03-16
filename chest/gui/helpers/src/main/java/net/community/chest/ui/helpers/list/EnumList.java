/*
 *
 */
package net.community.chest.ui.helpers.list;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>A {@link TypedList} with an {@link Enum} value associated with each row</P>
 *
 * @param <E> The used {@link Enum} value
 * @author Lyor G.
 * @since Dec 4, 2008 1:04:58 PM
 */
public class EnumList<E extends Enum<E>> extends TypedList<E> {
    /**
     *
     */
    private static final long serialVersionUID = 5448260244731774945L;
    private E[]    _vals    /* =null */;
    public synchronized E[] getEnumValues ()
    {
        if (null == _vals)
        {
            final TypedListModel<E>    model=getModel();
            if (model instanceof EnumListModel<?>)
                _vals = ((EnumListModel<E>) model).getEnumValues();
            else
                _vals = getValuesClass().getEnumConstants();
        }

        return _vals;
    }

    public void setEnumValues (E[] vals)
    {
        _vals = vals;
    }
    /**
     * Adds all available enumeration values. <B>Caveat emptor:</B> if called
     * more than once then the same values are re-added (<U>duplicates</U>).
     */
    public void populate ()
    {
        addValues(getEnumValues());
    }
    /**
     * Creates an optionally populated list
     * @param valsClass enum {@link Class} instance
     * @param autoPopulate if TRUE the {@link #populate()} method is called
     */
    public EnumList (Class<E> valsClass, boolean autoPopulate)
    {
        super(valsClass);

        if (autoPopulate)
            populate();
    }
    /**
     * Creates an un-populated list
     * @param valsClass enum {@link Class} instance
     * @see #EnumList(Class, boolean)
     */
    public EnumList (Class<E> valsClass)
    {
        this(valsClass, false);
    }
    /**
     * Creates an optionally populated list
     * @param aModel underlying model to use
     * @param autoPopulate if TRUE the {@link #populate()} method is called.
     * <B>Caveat emptor:</B> if underlying model already populated (even
     * partially) then <U>duplicate</U> entries will occur
     */
    public EnumList (TypedListModel<E> aModel, boolean autoPopulate)
    {
        super(aModel);

        if (autoPopulate)
            populate();
    }
    /**
     * Creates an un-populated list
     * @param aModel underlying model to use
     * @see #EnumList(TypedListModel, boolean)
     */
    public EnumList (TypedListModel<E> aModel)
    {
        this(aModel, false);
    }
}
