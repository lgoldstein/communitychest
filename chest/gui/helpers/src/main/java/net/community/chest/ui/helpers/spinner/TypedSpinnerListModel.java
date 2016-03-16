/*
 *
 */
package net.community.chest.ui.helpers.spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.SpinnerListModel;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.swing.models.TypedDisplayModeler;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> Type of associated value
 * @author Lyor G.
 * @since Dec 16, 2008 1:46:46 PM
 */
public class TypedSpinnerListModel<V> extends SpinnerListModel
            implements TypedValuesContainer<V>, TypedDisplayModeler<V> {
    /**
     *
     */
    private static final long serialVersionUID = -8954436896470969617L;
    private final Class<V>    _valsClass;
    /*
     * @see net.community.chest.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final /* no cheating */ Class<V> getValuesClass ()
    {
        return _valsClass;
    }

    private boolean    _cyclic;
    public boolean isCyclic ()
    {
        return _cyclic;
    }

    public void setCyclic (boolean cyclic)
    {
        _cyclic = cyclic;
    }

    @SuppressWarnings("unchecked")
    private static final List<?>    EMPTY_LIST=Arrays.asList(new MapEntryImpl<String,Object>("empty", null));
    public TypedSpinnerListModel (Class<V> valsClass, boolean cyclic)
    {
        super(EMPTY_LIST);

        if (null == (_valsClass=valsClass))
            throw new IllegalArgumentException("No values class specified");
        _cyclic = cyclic;
    }

    public TypedSpinnerListModel (Class<V> valsClass)
    {
        this(valsClass, false);
    }
    /*
     * @see javax.swing.SpinnerListModel#getList()
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Map.Entry<String,V>> getList ()
    {
        // trick to force a new list creation on "addItem" first call
        final List<?>    l=super.getList();
        if (l == EMPTY_LIST)
            return null;

        return (List<Map.Entry<String,V>>) l;
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#addItem(java.util.Map.Entry)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void addItem (Map.Entry<String,? extends V> item) throws IllegalArgumentException
    {
        // trick to force a new list creation on "addItem" first call
        List<Map.Entry<String,V>>    l=getList();
        final boolean                l1st=(null == l);
        if (l1st)
            l = new ArrayList<Map.Entry<String,V>>();

        l.add((Map.Entry<String,V>) item);

        if (l1st)
            setList(l);
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#addItem(java.lang.String, java.lang.Object)
     */
    @Override
    public Map.Entry<String,V> addItem (String text, V value) throws IllegalArgumentException
    {
        final Map.Entry<String,V>    item=new TypedSpinnerEntry<V>(text, value);
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
    public Map.Entry<String,V> getElementAt (final int index)
    {
        final List<? extends Map.Entry<String,V>>    l=getList();
        final int                                    numElems=(null == l) ? 0 : l.size();
        if ((index > 0) && (index < numElems))
            return l.get(0);

        return null;
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
     * @see net.community.chest.swing.models.TypedDisplayModeler#getItemAt(int)
     */
    @Override
    public Map.Entry<String, V> getItemAt (final int index)
    {
        return getElementAt(index);
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#getItemValue(int)
     */
    @Override
    public V getItemValue (final int index)
    {
        final Map.Entry<String,V>    item=getElementAt(index);
        return (null == item) /* can happen if invalid index */ ? null : item.getValue();
    }
    /*
     * @see net.community.chest.swing.models.TypedDisplayModeler#getItemText(int)
     */
    @Override
    public String getItemText (final int index)
    {
        final Map.Entry<String, V>    item=getElementAt(index);
        return (null == item) /* can happen if invalid index */ ? null : item.getKey();
    }
    /*
     * @see net.community.chest.swing.models.SingleSelectionModeler#getItemCount()
     */
    @Override
    public int getItemCount ()
    {
        final List<?>    l=getList();
        return (null == l) ? 0 : l.size();
    }

    @SuppressWarnings("unchecked")
    protected Map.Entry<String,V> extractAssignedValue (Object v)
    {
        if (v instanceof Map.Entry)
            return (Map.Entry<String,V>) v;
        else if (null == v)
            return null;
        else
            return new MapEntryImpl<String,V>(v.toString(), null);
    }
    /*
     * @see javax.swing.SpinnerListModel#getValue()
     */
    @Override
    @CoVariantReturn
    public Map.Entry<String,V> getValue ()
    {
        return extractAssignedValue(super.getValue());
    }
    /*
     * @see javax.swing.SpinnerListModel#setValue(java.lang.Object)
     */
    @Override
    public void setValue (Object v)
    {
        if (v instanceof Map.Entry<?,?>)
            super.setValue(v);
        else
            super.setValue(new TypedSpinnerEntry<Object>(String.valueOf(v), v));
    }
    /*
     * @see javax.swing.SpinnerListModel#getNextValue()
     */
    @Override
    @CoVariantReturn
    public Map.Entry<String,V> getNextValue ()
    {
        Object    v=super.getNextValue();
        if (isCyclic() && (null == v))
        {
            final List<? extends Map.Entry<?,?>>    l=getList();
            final int                                lSize=(null == l) ? 0 : l.size();
            if (lSize > 0)
            {
                v = l.get(0);
                setValue(v);
            }
        }

        return extractAssignedValue(v);
    }
    /*
     * @see javax.swing.SpinnerListModel#getPreviousValue()
     */
    @Override
    @CoVariantReturn
    public Map.Entry<String,V> getPreviousValue ()
    {
        Object    v=super.getPreviousValue();
        if (isCyclic() && (null == v))
        {
            final List<? extends Map.Entry<?,?>>    l=getList();
            final int                                lSize=(null == l) ? 0 : l.size();
            if (lSize > 0)
            {
                v = l.get(lSize - 1);
                setValue(v);
            }
        }

        return extractAssignedValue(v);
    }
}
