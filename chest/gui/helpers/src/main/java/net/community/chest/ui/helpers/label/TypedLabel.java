/*
 *
 */
package net.community.chest.ui.helpers.label;

import javax.swing.Icon;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.swing.component.label.BaseLabel;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> The assigned label value
 * @author Lyor G.
 * @since Dec 14, 2008 3:19:02 PM
 */
public class TypedLabel<V> extends BaseLabel
        implements TypedValuesContainer<V>, TypedComponentAssignment<V> {
    /**
     *
     */
    private static final long serialVersionUID = 912088427958096032L;
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

    public TypedLabel (Class<V> vc, V v, String text, Icon icon, int horizontalAlignment)
    {
        super(text, icon, horizontalAlignment);

        if (null == (_valsClass=vc))
            throw new IllegalArgumentException("No values class specified");

        _value = v;
    }

    public TypedLabel (Class<V> vc, V v, Icon image, int horizontalAlignment)
    {
        this(vc, v, null, image, horizontalAlignment);
    }

    public TypedLabel (Class<V> vc, Icon image, int horizontalAlignment)
    {
        this(vc, null, image, horizontalAlignment);
    }

    public TypedLabel (Class<V> vc, V v, Icon image)
    {
        this(vc, v, null, image, CENTER);
    }

    public TypedLabel (Class<V> vc, Icon image)
    {
        this(vc, null, null, image, CENTER);
    }

    public TypedLabel (Class<V> vc, V v, String text, int horizontalAlignment)
    {
        this(vc, v, text, null, horizontalAlignment);
    }

    public TypedLabel (Class<V> vc, String text, int horizontalAlignment)
    {
        this(vc, null, text, horizontalAlignment);
    }

    public TypedLabel (Class<V> vc, V v, String text)
    {
        this(vc, v, text, null, LEADING);
    }

    public TypedLabel (Class<V> vc, String text)
    {
        this(vc, null, text);
    }

    public TypedLabel (Class<V> vc, V v)
    {
        this(vc, v, "");
    }

    public TypedLabel (Class<V> vc)
    {
        this(vc, null, "");
    }
}
