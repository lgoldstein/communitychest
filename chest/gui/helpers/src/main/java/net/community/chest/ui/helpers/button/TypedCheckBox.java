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
 * @param <V> The contained value type
 * @author Lyor G.
 * @since Dec 11, 2008 9:54:42 AM
 */
public class TypedCheckBox<V> extends HelperCheckBox
			implements TypedValuesContainer<V>, TypedComponentAssignment<V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -140000597185543028L;
	private final Class<V>	_valsClass; 
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final Class<V> getValuesClass ()
	{
		return _valsClass;
	}

	private V	_value;
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
	 * @see net.community.chest.swing.component.button.BaseCheckBox#getCheckBoxConverter(org.w3c.dom.Element)
	 */
	@Override
	public XmlProxyConvertible<?> getCheckBoxConverter (Element elem) throws Exception
	{
		return (null == elem) ? null : TypedCheckBoxReflectiveProxy.TYPCB;
	}

	public TypedCheckBox (Class<V> vc, V v, Element elem, boolean autoLayout)
	{
		super(elem, false /* no-auto init until set other fields */);

		if (null == (_valsClass=vc))
			throw new IllegalArgumentException("No values class specified");
		_value = v;

		if (autoLayout)
			layoutComponent();
	}

	public TypedCheckBox (Class<V> vc, V v, Element elem)
	{
		this(vc, v, elem, true);
	}

	public TypedCheckBox (Class<V> vc, V v)
	{
		this(vc, v, null);
	}

	@SuppressWarnings("unchecked")
	public TypedCheckBox (V v)
	{
		this((null == v) ? null : (Class<V>) v.getClass(), v);
	}

	public TypedCheckBox (Class<V> vc, Element elem, boolean autoLayout)
	{
		this(vc, null, elem, autoLayout);
	}

	public TypedCheckBox (Class<V> vc, Element elem)
	{
		this(vc, elem, true);
	}

	public TypedCheckBox (Class<V> vc)
	{
		this(vc, (Element) null);
	}
}
