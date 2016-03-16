/*
 *
 */
package net.community.chest.ui.helpers.button;

import org.w3c.dom.Element;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.lang.TypedValuesContainer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> The assigned button value
 * @author Lyor G.
 * @since Dec 14, 2008 10:13:17 AM
 */
public class TypedButton<V> extends HelperButton
                implements TypedValuesContainer<V>, TypedComponentAssignment<V> {
    /**
     *
     */
    private static final long serialVersionUID = 8369323653779698759L;
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
    /*
     * @see net.community.chest.swing.component.button.BaseButton#getButtonConverter(org.w3c.dom.Element)
     */
    @Override
    protected XmlProxyConvertible<?> getButtonConverter (Element elem) throws Exception
    {
        return (null == elem) ? null : TypedButtonReflectiveProxy.TYPBTN;
    }

    public TypedButton (Class<V> vc, V v, Element elem, boolean autoLayout)
    {
        super(elem, false);    // no auto-layout till initialized the value

        if (null == (_valsClass=vc))
            throw new IllegalArgumentException("No values class specified");

        _value = v;

        if (autoLayout)
            layoutComponent();
    }

    public TypedButton (Class<V> vc, Element elem, boolean autoLayout)
    {
        this(vc, null, elem, autoLayout);
    }

    public TypedButton (Class<V> vc, Element elem)
    {
        this(vc, elem, true);
    }

    public TypedButton (Class<V> vc, V v)
    {
        this(vc, v, null, true);
    }

    public TypedButton (Class<V> vc)
    {
        this(vc, (Element) null);
    }

    @SuppressWarnings("unchecked")
    public TypedButton (V v, Element elem, boolean autoLayout)
    {
        this((null == v) ? null : (Class<V>) v.getClass(), v, elem, autoLayout);
    }

    public TypedButton (V v, Element elem)
    {
        this(v, elem, true);
    }

    public TypedButton (V v, boolean autoLayout)
    {
        this(v, null, autoLayout);
    }

    public TypedButton (V v)
    {
        this(v, true);
    }
}
