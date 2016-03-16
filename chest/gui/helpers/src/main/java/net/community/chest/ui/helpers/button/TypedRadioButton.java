/*
 *
 */
package net.community.chest.ui.helpers.button;

import javax.swing.JRadioButton;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.lang.TypedValuesContainer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> The assigned button value
 * @author Lyor G.
 * @since Dec 14, 2008 10:22:12 AM
 */
public class TypedRadioButton<V> extends JRadioButton
            implements TypedValuesContainer<V>, TypedComponentAssignment<V> {
    /**
     *
     */
    private static final long serialVersionUID = 6183944615357861910L;
    private final Class<V>    _valsClass;
    /*
     * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final Class<V> getValuesClass ()
    {
        return _valsClass;
    }

    private V    _value;
    /*
     * @see net.community.chest.ui.helpers.TypedComponentAssignment#getAssignedValue()
     */
    @Override
    public V getAssignedValue ()
    {
        return _value;
    }
    /*
     * @see net.community.chest.ui.helpers.TypedComponentAssignment#setAssignedValue(java.lang.Object)
     */
    @Override
    public void setAssignedValue (V v)
    {
        _value = v;
    }

    public TypedRadioButton (Class<V> vc, V v)
    {
        if (null == (_valsClass=vc))
            throw new IllegalArgumentException("No values class specified");

        _value = v;
    }

    public TypedRadioButton (Class<V> vc)
    {
        this(vc, null);
    }

    @SuppressWarnings("unchecked")
    public TypedRadioButton (V v)
    {
        this((null == v) ? null : (Class<V>) v.getClass(), v);
    }
}
