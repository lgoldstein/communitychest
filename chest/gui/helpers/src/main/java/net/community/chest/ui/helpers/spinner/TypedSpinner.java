/*
 * 
 */
package net.community.chest.ui.helpers.spinner;

import javax.swing.SpinnerModel;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.swing.component.spinner.BaseSpinnerNumberModel;

import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <V> Type of value being associated with the spinner
 * @author Lyor G.
 * @since Mar 12, 2009 8:00:06 AM
 *
 */
public class TypedSpinner<V> extends HelperSpinner implements TypedValuesContainer<V>, TypedComponentAssignment<V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5543341571408807183L;
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

	public TypedSpinner (Class<V> vc, V v, SpinnerModel model, Element elem, boolean autoLayout)
	{
		super(model, elem, false /* delay layout till initializing values class and value */);

		if (null == (_valsClass=vc))
			throw new IllegalArgumentException("No values class specified");

		_value = v;

		if (autoLayout)
			layoutComponent();
	}

	public TypedSpinner (Class<V> vc, V v, SpinnerModel model, Element elem)
	{
		this(vc, v, model, elem, true);
	}

	public TypedSpinner (Class<V> vc, V v, SpinnerModel model, boolean autoLayout)
	{
		this(vc, v, model, null, autoLayout);
	}

	public TypedSpinner (Class<V> vc, V v, SpinnerModel model)
	{
		this(vc, v, model, true);
	}

	public TypedSpinner (Class<V> vc, SpinnerModel model, boolean autoLayout)
	{
		this(vc, null, model, autoLayout);
	}

	public TypedSpinner (Class<V> vc, SpinnerModel model)
	{
		this(vc, model, true);
	}

	public TypedSpinner (Class<V> vc, V v, boolean autoLayout)
	{
		this(vc, v, new BaseSpinnerNumberModel(), autoLayout);
	}

	public TypedSpinner (Class<V> vc, V v)
	{
		this(vc, v, true);
	}

	public TypedSpinner (Class<V> vc, boolean autoLayout)
	{
		this(vc, (V) null, autoLayout);
	}

	public TypedSpinner (Class<V> vc)
	{
		this(vc, true);
	}

	@SuppressWarnings("unchecked")
	public TypedSpinner (V v, SpinnerModel model, Element elem, boolean autoLayout)
	{
		this((null == v) ? null : (Class<V>) v.getClass(), v, model, elem, autoLayout);
	}

	public TypedSpinner (V v, Element elem, boolean autoLayout)
	{
		this(v, new BaseSpinnerNumberModel(), elem, autoLayout);
	}

	public TypedSpinner (V v, Element elem)
	{
		this(v, elem, true);
	}

	public TypedSpinner (V v, SpinnerModel model, Element elem)
	{
		this(v, model, elem, true);
	}

	public TypedSpinner (V v, SpinnerModel model, boolean autoLayout)
	{
		this(v, model, null, autoLayout);
	}

	public TypedSpinner (V v, SpinnerModel model)
	{
		this(v, model, true);
	}

	@SuppressWarnings("unchecked")
	public TypedSpinner (V v, boolean autoLayout)
	{
		this((null == v) ? null : (Class<V>) v.getClass(), v, autoLayout);
	}

	public TypedSpinner (V v)
	{
		this(v, true);
	}
}
