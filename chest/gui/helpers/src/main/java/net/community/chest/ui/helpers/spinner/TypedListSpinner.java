/*
 * 
 */
package net.community.chest.ui.helpers.spinner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.swing.JSpinner;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.LocalizedComponent;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.swing.models.TypedDisplayModeler;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> Type of value associated with each choice
 * @author Lyor G.
 * @since Dec 16, 2008 2:13:13 PM
 */
public class TypedListSpinner<V> extends JSpinner 
		implements TypedValuesContainer<V>, TypedDisplayModeler<V>, LocalizedComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2519297383687940532L;
	public TypedListSpinner (TypedSpinnerListModel<V> model)
	{
		super(model);
	}

	public TypedListSpinner (Class<V> vc)
	{
		this(new TypedSpinnerListModel<V>(vc));
	}
	/*
	 * @see javax.swing.JSpinner#getModel()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public TypedSpinnerListModel<V> getModel ()
	{
		return (TypedSpinnerListModel<V>) super.getModel();
	}
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final Class<V> getValuesClass ()
	{
		final TypedSpinnerListModel<V>	m=getModel();
		return (null == m) ? null : m.getValuesClass();
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItem(java.lang.String, java.lang.Object)
	 */
	@Override
	public Map.Entry<String,V> addItem (final String name, final V value) throws IllegalArgumentException
	{
		return getModel().addItem(name, value);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getValueDisplayText(java.lang.Object)
	 */
	@Override
	public String getValueDisplayText (final V value)
	{
		return getModel().getValueDisplayText(value); 
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addValue(java.lang.Object)
	 */
	@Override
	public Map.Entry<String,V> addValue (final V value)
	{
		return addItem(getValueDisplayText(value), value);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addValues(java.util.Collection)
	 */
	@Override
	public void addValues (final Collection<? extends V> vals)
	{
		if ((vals != null) && (vals.size() > 0))
		{
			for (final V value : vals)
				addValue(value);
		}
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addValues(V[])
	 */
	@Override
	public void addValues (final V ... vals)
	{
		if ((vals != null) && (vals.length > 0))
			addValues(Arrays.asList(vals));
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItem(java.util.Map.Entry)
	 */
	@Override
	public void addItem (Map.Entry<String, ? extends V> item) throws IllegalArgumentException
	{
		getModel().addItem(item);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Collection)
	 */
	@Override
	public void addItems (final Collection<? extends Map.Entry<String,? extends V>> items)
	{
		getModel().addItems(items);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Map.Entry<java.lang.String,V>[])
	 */
	@Override
	public void addItems (final Map.Entry<String,V> ... items)
	{
		getModel().addItems(items);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Map)
	 */
	@Override
	public void addItems (final Map<String,? extends V> items)
	{
		getModel().addItems(items);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getItemText(int)
	 */
	@Override
	public String getItemText (int index)
	{
		return getModel().getItemText(index);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getItemValue(int)
	 */
	@Override
	public V getItemValue (int index)
	{
		return getModel().getItemValue(index);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getItemAt(int)
	 */
	@Override
	public Map.Entry<String,V> getItemAt (int index)
	{
		return getModel().getItemAt(index);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getItemCount()
	 */
	@Override
	public int getItemCount ()
	{
		return getModel().getItemCount();
	}
	@SuppressWarnings("unchecked")
	protected Map.Entry<String,V> extractAssignedValue (Object v)
	{
		return (null == v) ? null : (Map.Entry<String,V>) v;
	}
	/*
	 * @see javax.swing.JSpinner#getNextValue()
	 */
	@Override
	@CoVariantReturn
	public Map.Entry<String,V> getNextValue ()
	{
		return extractAssignedValue(super.getNextValue());
	}
	/*
	 * @see javax.swing.JSpinner#getPreviousValue()
	 */
	@Override
	@CoVariantReturn
	public Map.Entry<String,V> getPreviousValue ()
	{
		return extractAssignedValue(super.getPreviousValue());
	}
	/*
	 * @see javax.swing.JSpinner#getValue()
	 */
	@Override
	@CoVariantReturn
	public Map.Entry<String,V> getValue ()
	{
		return extractAssignedValue(super.getValue());
	}

	private Locale	_lcl	/* =null */;
	/*
	 * @see net.community.chest.ui.helpers.LocalizedComponent#getDisplayLocale()
	 */
	@Override
	public synchronized Locale getDisplayLocale ()
	{
		if (null == _lcl)
			_lcl = Locale.getDefault();
		return _lcl;
	}
	/*
	 * @see net.community.chest.ui.helpers.LocalizedComponent#setDisplayLocale(java.util.Locale)
	 */
	@Override
	public synchronized void setDisplayLocale (Locale l)
	{
		if (_lcl != l)	// debug breakpoint
			_lcl = l;
	}
}
