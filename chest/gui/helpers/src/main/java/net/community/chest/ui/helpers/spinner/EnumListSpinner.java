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
 * @param <E> The associated {@link Enum} type
 * @author Lyor G.
 * @since Dec 16, 2008 2:25:23 PM
 */
public class EnumListSpinner<E extends Enum<E>> extends TypedListSpinner<E> {
    /**
     *
     */
    private static final long serialVersionUID = -8329807132783513442L;
    private List<E>    _vals    /* =null */;
    public synchronized List<E> getEnumValues ()
    {
        if (null == _vals)
        {
            final TypedSpinnerListModel<E>    model=getModel();
            if (model instanceof EnumSpinnerListModel<?>)
                _vals = ((EnumSpinnerListModel<E>) model).getEnumValues();
            else
                _vals = Collections.unmodifiableList(Arrays.asList(getValuesClass().getEnumConstants()));
        }

        return _vals;
    }

    public void setEnumValues (List<E> vals)
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
     * Creates an optionally populated spinner
     * @param valsClass enum {@link Class} instance
     * @param autoPopulate if TRUE the {@link #populate()} method is called
     */
    public EnumListSpinner (Class<E> valsClass, boolean autoPopulate)
    {
        super(valsClass);

        if (autoPopulate)
            populate();
    }
    /**
     * Creates an un-populated spinner
     * @param valsClass enum {@link Class} instance
     * @see #EnumListSpinner(Class, boolean)
     */
    public EnumListSpinner (Class<E> valsClass)
    {
        this(valsClass, false);
    }
    /**
     * Creates an optionally populated spinner
     * @param aModel underlying model to use
     * @param autoPopulate if TRUE the {@link #populate()} method is called.
     * <B>Caveat emptor:</B> if underlying model already populated (even
     * partially) then <U>duplicate</U> entries will occur
     */
    public EnumListSpinner (TypedSpinnerListModel<E> aModel, boolean autoPopulate)
    {
        super(aModel);

        if (autoPopulate)
            populate();
    }
    /**
     * Creates an un-populated spinner
     * @param aModel underlying model to use
     */

    public EnumListSpinner (TypedSpinnerListModel<E> aModel)
    {
        this(aModel, false);
    }
}
