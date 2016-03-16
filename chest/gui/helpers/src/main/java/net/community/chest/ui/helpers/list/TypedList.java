/*
 *
 */
package net.community.chest.ui.helpers.list;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.swing.component.list.BaseList;
import net.community.chest.swing.models.SingleSelectionModeler;
import net.community.chest.swing.models.TypedDisplayModeler;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> The associated value type for each row
 * @author Lyor G.
 * @since Dec 4, 2008 12:39:57 PM
 */
public class TypedList<V> extends BaseList
        implements TypedValuesContainer<V>,
                   TypedDisplayModeler<V>,
                   SingleSelectionModeler<V> {
    /**
     *
     */
    private static final long serialVersionUID = 162582819548702619L;

    /**
     * @param aModel underlying model to be used
     */
    public TypedList (TypedListModel<V> aModel)
    {
        super(aModel);
    }
    /**
     * Generates an automatic {@link TypedListModel} for the list
     * @param valsClass class of values for the generated {@link TypedListModel}
     */
    public TypedList (Class<V> valsClass)
    {
        this(new TypedListModel<V>(valsClass));
    }
    /*
     * @see javax.swing.JList#getModel()
     */
    @SuppressWarnings("unchecked")
    @Override
    @CoVariantReturn
    public TypedListModel<V> getModel ()
    {
        return (TypedListModel<V>) super.getModel();
    }
    /*
     * @see net.community.chest.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final /* no cheating */ Class<V> getValuesClass ()
    {
        return getModel().getValuesClass();
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
    /*
     * @see net.community.chest.swing.models.SingleSelectionModeler#getSelectedItem()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map.Entry<String,V> getSelectedItem ()
    {
        return (Map.Entry<String,V>) super.getSelectedValue();
    }
    /*
     * @see net.community.chest.swing.models.SingleSelectionModeler#getSelectedText()
     */
    @Override
    public String getSelectedText ()
    {
        final Map.Entry<String,V>    selElem=getSelectedItem();
        return (null == selElem) ? null : selElem.getKey();
    }
    /*
     * @see net.community.chest.swing.models.SingleSelectionModeler#setSelectedValue(java.lang.Object)
     */
    @Override
    public int setSelectedValue (V value)
    {
        throw new UnsupportedOperationException("setSelectedValue(" + value + ") TODO");
    }
    /*
     * @see javax.swing.JList#getSelectedValue()
     */
    @Override
    @CoVariantReturn
    public V getSelectedValue ()
    {
        final Map.Entry<String,V>    selElem=getSelectedItem();
        return (null == selElem) ? null : selElem.getValue();
    }

    public Map.Entry<String,V>[] getSelectedItems ()
    {
        final Object[]    a=super.getSelectedValues();
        if ((null == a) || (a.length <= 0))
            return null;

        @SuppressWarnings("unchecked")
        final Map.Entry<String,V>[]    items=
            (Map.Entry[]) Array.newInstance(Map.Entry.class, a.length);
        for (int    vIndex=0; vIndex < a.length; vIndex++)
        {
            @SuppressWarnings("unchecked")
            final Map.Entry<String,V>    ie=(Map.Entry<String,V>) a[vIndex];
            items[vIndex] = ie;
        }
        return items;
    }
    /*
     * @see javax.swing.JList#getSelectedValues()
     */
    @SuppressWarnings("unchecked")
    @Override
    @CoVariantReturn
    public V[] getSelectedValues ()
    {
        final Map.Entry<String,V>[]    a=getSelectedItems();
        if ((null == a) || (a.length <= 0))
            return null;

        final V[]    rv=(V[]) Array.newInstance(getValuesClass(), a.length);
        for (int    vIndex=0; vIndex < a.length; vIndex++)
        {
            final Map.Entry<String,V>    elem=a[vIndex];
            final V                        selElem=(null == elem) ? null : elem.getValue();
            rv[vIndex] = selElem;
        }

        return rv;
    }

    public List<V> toValuesList ()
    {
        return getModel().toValuesList();
    }
}
