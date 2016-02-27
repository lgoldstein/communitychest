/*
 * 
 */
package net.community.chest.ui.helpers.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.swing.models.TypedDisplayModeler;
import net.community.chest.ui.helpers.combobox.TypedComboBoxEntry;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> The of value contained in the model
 * @author Lyor G.
 * @since Dec 4, 2008 12:12:29 PM
 */
public class TypedListModel<V> extends DefaultListModel
		implements TypedValuesContainer<V>,
				   TypedDisplayModeler <V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7881156492386689334L;
	private final Class<V>	_valsClass;
	/*
	 * @see net.community.chest.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final /* no cheating */ Class<V> getValuesClass ()
	{
		return _valsClass;
	}
	/**
	 * @param valsClass {@link Class} representing the expected items type
	 * associated with each entry in the list
	 * @throws IllegalArgumentException if null values {@link Class} instance
	 * supplied as parameter
	 */
	public TypedListModel (final Class<V> valsClass) throws IllegalArgumentException
	{
		if (null == (_valsClass=valsClass))
			throw new IllegalArgumentException("no values class supplied");
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItem(java.util.Map.Entry)
	 */
	@Override
	public void addItem (final Map.Entry<String,? extends V> item)
	{
		if (item != null)
			super.addElement(item);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItem(java.lang.String, java.lang.Object)
	 */
	@Override
	public Map.Entry<String,V> addItem (final String text, final V value) throws IllegalArgumentException
	{
		if ((null == text) || (text.length() <= 0))
			throw new IllegalArgumentException("null/empty combo box item name");

		final Map.Entry<String,V>	item=new TypedComboBoxEntry<V>(text, value);
		addItem(item);
		return item;
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Collection)
	 */
	@Override
	public void addItems (final Collection<? extends Map.Entry<String,? extends V>> items)
	{
		if ((items != null) && (items.size() > 0))
		{
			for (final Map.Entry<String,? extends V> e : items)
			{
				if (e != null)
					addItem(e);
			}
		}
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Map)
	 */
	@Override
	public void addItems (final Map<String,? extends V> items)
	{
		if ((items != null) && (items.size() > 0))
			addItems(items.entrySet());
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addItems(java.util.Map.Entry<java.lang.String,V>[])
	 */
	@Override
	public void addItems (final Map.Entry<String, V> ... items)
	{
		if ((items != null) && (items.length > 0))
			addItems(Arrays.asList(items));
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getValueDisplayText(java.lang.Object)
	 */
	@Override
	public String getValueDisplayText (final V value)
	{
		return (null == value) ? null : value.toString();
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#addValue(java.lang.Object)
	 */
	@Override
	public Map.Entry<String,V> addValue (V value)
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
	/* NOTE: returns null if invalid index
	 * @see javax.swing.DefaultComboBoxModel#getElementAt(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	@CoVariantReturn
	public Map.Entry<String,V> getElementAt (final int index)
	{
		if ((index < 0) || (index >= getSize()))
			return null;
		return (Map.Entry<String, V>) super.getElementAt(index);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getItemAt(int)
	 */
	@Override
	public Map.Entry<String,V> getItemAt (final int index)
	{
		return getElementAt(index);
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getItemValue(int)
	 */
	@Override
	public V getItemValue (final int index)
	{
		final Map.Entry<String, V>	item=getElementAt(index);
		return (null == item) /* can happen if invalid index */ ? null : item.getValue();
	}

	public List<V> toValuesList ()
	{
		final int	numItems=getSize();
		if (numItems <= 0)
			return null;

		final List<V>	vList=new ArrayList<V>(numItems);
		for (int	vIndex=0; vIndex < numItems; vIndex++)
		{
			final V	v=getItemValue(vIndex);
			if (v == null)
				continue;
			if (!vList.add(v))
				continue;	// debug breakpoint
		}

		return vList;
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getItemText(int)
	 */
	@Override
	public String getItemText (final int index)
	{
		final Map.Entry<String, V>	item=getElementAt(index);
		return (null == item) /* can happen if invalid index */ ? null : item.getKey();
	}
	/*
	 * @see net.community.chest.swing.models.TypedDisplayModeler#getItemCount()
	 */
	@Override
	public int getItemCount ()
	{
		return getSize();
	}
	/*
	 * @see javax.swing.DefaultListModel#copyInto(java.lang.Object[])
	 */
	@Override
	public void copyInto (Object anArray[])
	{
		throw new UnsupportedOperationException("copyInto() N/A");
	}
	/*
	 * @see javax.swing.DefaultListModel#elements()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Enumeration<? extends Map.Entry<String,V>> elements()
	{
		return super.elements();
	}
	/*
	 * @see javax.swing.DefaultListModel#contains(java.lang.Object)
	 */
	@Override
	public boolean contains (Object elem)
	{
		throw new UnsupportedOperationException("contains(" + elem + ") N/A");
	}
	/*
	 * @see javax.swing.DefaultListModel#indexOf(java.lang.Object, int)
	 */
	@Override
	public int indexOf (Object elem, int index)
	{
		throw new UnsupportedOperationException("indexOf(" + elem + ")[index=" + index + "] N/A");
	}

	public int valueIndexOf (V value)
	{
		return valueIndexOf(value, null);
	}

	public int valueIndexOf (V value, Comparator<? super V> c)
	{
		return valueIndexOf(value, c, 0);
	}

	public int valueIndexOf (V value, int index)
	{
		return valueIndexOf(value, null, index);
	}

	public int valueIndexOf (V value, Comparator<? super V> c, int index)
	{
		if (value == null)
			return Integer.MIN_VALUE;

		final int	numItems=getItemCount();
		for (int	vIndex=index; vIndex < numItems; vIndex++)
		{
			final V	cv=getItemValue(vIndex);
			if (cv == null)
				continue;
			if (c == null)
			{
				if (value.equals(cv))
					return vIndex;
			}
			else if (c.compare(value, cv) == 0)
				return vIndex;
		}

		return (-1);
	}
	/*
	 * @see javax.swing.DefaultListModel#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf (Object elem)
	{
		return indexOf(elem, 0);
	}
	/*
	 * @see javax.swing.DefaultListModel#lastIndexOf(java.lang.Object, int)
	 */
	@Override
	public int lastIndexOf (Object elem, int index)
	{
		throw new UnsupportedOperationException("lastIndexOf(" + elem + ")[index=" + index + "] N/A");
	}
	/*
	 * @see javax.swing.DefaultListModel#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf (Object elem)
	{
		return lastIndexOf(elem, 0);
	}
	/*
	 * @see javax.swing.DefaultListModel#firstElement()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@CoVariantReturn
	public Map.Entry<String,V> firstElement ()
	{
		return (Map.Entry<String,V>) super.firstElement(); 
	}
	/*
	 * @see javax.swing.DefaultListModel#lastElement()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@CoVariantReturn
	public Map.Entry<String,V> lastElement ()
	{
		return (Map.Entry<String,V>) super.lastElement(); 
	}
	/*
	 * @see javax.swing.DefaultListModel#setElementAt(java.lang.Object, int)
	 */
	@Override
	public void setElementAt (Object obj, int index)
	{
		throw new UnsupportedOperationException("setElementAt(" + index + ")[" + obj + "] N/A");
	}
	/*
	 * @see javax.swing.DefaultListModel#insertElementAt(java.lang.Object, int)
	 */
	@Override
	public void insertElementAt (Object obj, int index)
	{
		throw new UnsupportedOperationException("insertElementAt(" + index + ")[" + obj + "] N/A");
	}
	/*
	 * @see javax.swing.DefaultListModel#addElement(java.lang.Object)
	 */
	@Override
	public void addElement (Object obj)
	{
		throw new UnsupportedOperationException("addElement(" + obj + ") N/A");
	}
	/*
	 * @see javax.swing.DefaultListModel#removeElement(java.lang.Object)
	 */
	@Override
	public boolean removeElement (Object obj)
	{
		throw new UnsupportedOperationException("removeElement(" + obj + ") N/A");
	}
	/*
	 * @see javax.swing.DefaultListModel#toArray()
	 */
	@Override
	@CoVariantReturn
	public Map.Entry<String,V>[] toArray ()
	{
		final Object[]	a=super.toArray();
		if ((null == a) || (a.length <= 0))
			return null;

		final Collection<Map.Entry<String,V>>	al=new ArrayList<Map.Entry<String,V>>(a.length);
		for (final Object o : a)
		{
			if (o instanceof Map.Entry<?,?>)
			{
				@SuppressWarnings("unchecked")
				final Map.Entry<String,V>	oe=(Map.Entry<String,V>) o;
				al.add(oe);
			}
		}

		final int					numEntries=al.size();
		@SuppressWarnings("unchecked")
		final Map.Entry<String,V>[]	ea=
			(numEntries <= 0) ? null : al.toArray(new Map.Entry[numEntries]);
		return ea;
	}
	/*
	 * @see javax.swing.DefaultListModel#get(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	@CoVariantReturn
	public Map.Entry<String,V> get (int index)
	{
		return (Map.Entry<String,V>) super.get(index);
	}
	/*
	 * @see javax.swing.DefaultListModel#set(int, java.lang.Object)
	 */
	@Override
	public Object set (int index, Object element)
	{
		throw new UnsupportedOperationException("set(" + index + ")[" + element + "] N/A");
	}
	/*
	 * @see javax.swing.DefaultListModel#add(int, java.lang.Object)
	 */
	@Override
	public void add (int index, Object element)
	{
		throw new UnsupportedOperationException("add(" + index + ")[" + element + "] N/A");
	}
	/*
	 * @see javax.swing.DefaultListModel#remove(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	@CoVariantReturn
	public Map.Entry<String,V> remove (int index)
	{
		return (Map.Entry<String,V>) super.remove(index);
	}
}
