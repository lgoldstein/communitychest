package net.community.chest.ui.helpers.table;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.table.AbstractTableModel;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Implements a "typed" {@link AbstractTableModel} where each row is
 * assumed to be represented via an object. The model also serves as a
 * {@link List} of rows enabling easy addition/removal/location of the
 * table's rows</P>
 *
 * @param <V> The type of value associated with each row
 * @author Lyor G.
 * @since Aug 6, 2007 7:57:27 AM
 */
public abstract class AbstractTypedTableModel<V> extends AbstractTableModel implements TypedTableModel<V> {
    /**
     *
     */
    private static final long serialVersionUID = 3557389669545151686L;
    private final Class<V>    _valsClass;
    /*
     * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final /* no cheating */ Class<V> getValuesClass ()
    {
        return _valsClass;
    }

    private final List<V>    _dataRows;
    protected AbstractTypedTableModel (final Class<V> valsClass, final int initialSize) throws IllegalArgumentException
    {
        if (null == (_valsClass=valsClass))
            throw new IllegalArgumentException("No values class instance");

        _dataRows = (initialSize <= 0) ? new ArrayList<V>() : new ArrayList<V>(initialSize);
    }

    protected AbstractTypedTableModel (Class<V> valsClass) throws IllegalArgumentException
    {
        this(valsClass, 0);
    }
    /*
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount ()
    {
        return size();
    }
    /**
     * <P>Called by default {@link #getValueAt(int, int)} implementation in
     * order to retrieve the display object for the specified column of the
     * row value.</P>
     * @param rowIndex zero based row index
     * @param row associated row value object
     * @param colIndex column index
     * @return {@link Object} to be displayed for the requested row/column
     */
    public abstract Object getColumnValue (final int rowIndex, final V row, final int colIndex);
    /*
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt (int rowIndex, int columnIndex)
    {
        return getColumnValue(rowIndex, get(rowIndex), columnIndex);
    }
    /**
     * <P>Called by default {@link #setValueAt(Object, int, int)}
     * implementation in order to update the associated row value (if
     * editing is allowed of course)</P>
     * @param rowIndex zero based row index
     * @param row associated row value
     * @param colIndex column index
     * @param value value to be set for the specified column
     */
    public abstract void setValueAt (final int rowIndex, final V row, final int colIndex, final Object value);
    /*
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt (Object value, int rowIndex, int columnIndex)
    {
        setValueAt(rowIndex, get(rowIndex), columnIndex, value);
    }

    public boolean isCellEditable (int rowIndex, V row, int columnIndex)
    {
        return (row != null) && super.isCellEditable(rowIndex, columnIndex);
    }
    /*
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable (int rowIndex, int columnIndex)
    {
        return isCellEditable(rowIndex, get(rowIndex), columnIndex);
    }

    protected void add (List<V> rows, int index, V element)
    {
        if (index == rows.size())    // check if adding at end of list
            rows.add(element);
        else
            rows.add(index, element);
    }

    protected void add (List<V> rows, int index, V element, boolean fireEvent)
    {
        add(rows, index, element);
        if (fireEvent)
            fireTableRowsInserted(index, index);
    }

    public void add (int index, V element, boolean fireEvent)
    {
        add(_dataRows, index, element, fireEvent);
    }
    /*
     * @see java.util.List#add(int, java.lang.Object)
     */
    @Override
    public void add (int index, V element)
    {
        add(index, element, true);
    }

    protected boolean add (List<V> rows, V o)
    {
        add(rows, size(), o);
        return true;
    }

    protected boolean add (List<V> rows, V o, boolean fireEvent)
    {
        final boolean    added=add(rows, o);
        if (added && fireEvent)
        {
            final int    rowIndex=rows.size() - 1;
            if (rowIndex >= 0)
                fireTableRowsInserted(rowIndex, rowIndex);
        }

        return added;
    }

    public boolean add (V o, boolean fireEvent)
    {
        return add(_dataRows, o, fireEvent);
    }
    /*
     * @see java.util.List#add(java.lang.Object)
     */
    @Override
    public boolean add (V o)
    {
        return add(o, true);
    }

    protected boolean addAll (List<V> rows, Collection<? extends V> c)
    {
        return addAll(rows, size(), c);
    }

    protected int getMaxRowIndex (List<V> rows)
    {
        return rows.size();
    }

    protected boolean addAll (List<V> rows, Collection<? extends V> c, boolean fireEvent)
    {
        if ((null == c) || (c.size() <= 0))
            return false;

        final int        prevNum=getMaxRowIndex(rows);
        final boolean    added=addAll(rows, c);
        if (added && fireEvent)
        {
            final int    curNum=getMaxRowIndex(rows);
            if (curNum > prevNum)
                fireTableRowsInserted(prevNum, curNum-1);
        }

        return added;

    }

    public boolean addAll (Collection<? extends V> c, boolean fireEvent)
    {
        return addAll(_dataRows, c, fireEvent);
    }
    /*
     * @see java.util.List#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll (Collection<? extends V> c)
    {
        return addAll(c, true);
    }

    protected boolean addAll (List<V> rows, int index, Collection<? extends V> c)
    {
        if (index == rows.size())    // check if adding to end of list
            return rows.addAll(c);
        else
            return rows.addAll(index, c);
    }

    protected boolean addAll (List<V> rows, int index, Collection<? extends V> c, boolean fireEvent)
    {
        if ((null == c) || (c.size() <= 0))
            return false;

        final boolean    added=addAll(rows, index, c);
        if (added && fireEvent)
        {
            final int    curNum=getMaxRowIndex(rows),
                        lastNum=index + c.size(),
                        maxNum=Math.min(curNum, lastNum);
            if (maxNum > index)
                fireTableRowsInserted(index, maxNum-1);
        }

        return added;

    }

    public boolean addAll (int index, Collection<? extends V> c, boolean fireEvent)
    {
        return addAll(_dataRows, index, c, fireEvent);
    }
    /*
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    @Override
    public boolean addAll (int index, Collection<? extends V> c)
    {
        return addAll(index, c, true);
    }

    protected void clear (List<V> rows)
    {
        rows.clear();
    }

    protected void clear (List<V> rows, boolean fireEvent)
    {
        final int    numRows=getMaxRowIndex(rows);
        if (numRows > 0)
        {
            clear(rows);

            if (fireEvent)
                fireTableRowsDeleted(0, numRows - 1);
        }
    }

    public void clear (boolean fireEvent)
    {
        clear(_dataRows, fireEvent);
    }
    /*
     * @see java.util.List#clear()
     */
    @Override
    public void clear ()
    {
        clear(true);
    }
    // check classes compatibility
    protected boolean isValidObject (Object o)
    {
        final Class<?>    oClass=(null == o) /* OK */ ? null : o.getClass();
        final Class<V>    vClass=getValuesClass();
        if ((null == oClass) || (null == vClass) || (!vClass.isAssignableFrom(oClass)))
            return false;    // debug breakpoint

        return true;
    }

    protected boolean contains (List<V> rows, Object o)
    {
        if (!isValidObject(o))
            return false;

        return containsAll(rows, Arrays.asList(o));
    }
    /*
     * @see java.util.List#contains(java.lang.Object)
     */
    @Override
    public boolean contains (Object o)
    {
        return contains(_dataRows, o);
    }

    protected boolean containsAll (List<V> rows, Collection<?> c)
    {
        return rows.containsAll(c);
    }
    /*
     * @see java.util.List#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll (Collection<?> c)
    {
        return containsAll(_dataRows, c);
    }

    protected V get (List<V> rows, int index)
    {
        return rows.get(index);
    }
    /*
     * @see java.util.List#get(int)
     */
    @Override
    public V get (int index)
    {
        return get(_dataRows, index);
    }

    protected int indexOf (List<V> rows, Object o)
    {
        if (!isValidObject(o))
            return (-1);

        return rows.indexOf(o);
    }
    /*
     * @see java.util.List#indexOf(java.lang.Object)
     */
    @Override
    public int indexOf (Object o)
    {
        return indexOf(_dataRows, o);
    }
    /*
     * @see java.util.List#isEmpty()
     */
    @Override
    public boolean isEmpty ()
    {
        return (size() <= 0);
    }

    protected Iterator<V> iterator (List<V> rows)
    {
        return listIterator(rows, 0);
    }
    /*
     * @see java.util.List#iterator()
     */
    @Override
    public Iterator<V> iterator ()
    {
        return iterator(_dataRows);
    }

    protected int lastIndexOf (List<V> rows, Object o)
    {
        if (!isValidObject(o))
            return (-1);

        return rows.lastIndexOf(o);
    }
    /*
      * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    @Override
    public int lastIndexOf (Object o)
    {
        return lastIndexOf(_dataRows, o);
    }
    /*
     * @see java.util.List#listIterator()
     */
    @Override
    public ListIterator<V> listIterator ()
    {
        return listIterator(0);
    }

    protected ListIterator<V> listIterator (List<V> rows, int index)
    {
        return rows.listIterator(index);
    }
    /*
     * @see java.util.List#listIterator(int)
     */
    @Override
    public ListIterator<V> listIterator (int index)
    {
        return listIterator(_dataRows, index);
    }

    protected V remove (List<V> rows, int index)
    {
        return rows.remove(index);
    }

    protected V remove (List<V> rows, int index, boolean fireEvent)
    {
        final V    val=remove(rows, index);
        if ((val != null) && fireEvent)
            fireTableRowsDeleted(index, index);

        return val;
    }

    public V remove (int index, boolean fireEvent)
    {
        return remove(_dataRows, index, fireEvent);
    }
    /*
     * @see java.util.List#remove(int)
     */
    @Override
    public V remove (int index)
    {
        return remove(index, true);
    }

    public boolean remove (Object o, boolean fireEvent)
    {
        final int    index=indexOf(o);
        if ((index >= 0) && (index < size()))
        {
            remove(index, fireEvent);
            return true;
        }

        return false;
    }
    /*
     * @see java.util.List#remove(java.lang.Object)
     */
    @Override
    public boolean remove (Object o)
    {
        return remove(o, true);
    }

    protected boolean removeAll (List<V> rows, Collection<?> c)
    {
        if ((null == c) || (c.size() <= 0))
            return false;

        return rows.removeAll(c);
    }

    protected boolean removeAll (List<V> rows, Collection<?> c, boolean fireEvent)
    {
        final boolean    removed=removeAll(rows, c);
        // TODO check if need to send specific events for each removed object
        if (removed && fireEvent)
            fireTableDataChanged();

        return removed;
    }

    public boolean removeAll (Collection<?> c, boolean fireEvent)
    {
        return removeAll(_dataRows, c, fireEvent);
    }
    /*
     * @see java.util.List#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll (Collection<?> c)
    {
        return removeAll(c, true);
    }

    protected boolean retainAll (List<V> rows, Collection<?> c)
    {
        if ((null == c) || (c.size() <= 0))
            return false;

        return rows.retainAll(c);
    }

    protected boolean retainAll (List<V> rows, Collection<?> c, boolean fireEvent)
    {
        final boolean    changed=retainAll(rows, c);
        // TODO check if need to send specific events for each removed object
        if (changed && fireEvent)
            fireTableDataChanged();

        return changed;
    }

    public boolean retainAll (Collection<?> c, boolean fireEvent)
    {
        return retainAll(_dataRows, c, fireEvent);
    }
    /*
     * @see java.util.List#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll (Collection<?> c)
    {
        return retainAll(c, true);
    }

    protected V set (List<V> rows, int index, V element)
    {
        return rows.set(index, element);
    }

    protected V set (List<V> rows, int index, V element, boolean fireEvent)
    {
        final V    o=set(rows, index, element);
        if (fireEvent)
            fireTableRowsUpdated(index, index);

        return o;
    }

    public V set (int index, V element, boolean fireEvent)
    {
        return set(_dataRows, index, element, fireEvent);
    }
    /*
     * @see java.util.List#set(int, java.lang.Object)
     */
    @Override
    public V set (int index, V element)
    {
        return set(index, element, true);
    }
    /*
     * @see java.util.List#size()
     */
    @Override
    public int size ()
    {
        return getMaxRowIndex(_dataRows);
    }

    protected List<V> subList (List<V> rows, int fromIndex, int toIndex)
    {
        return rows.subList(fromIndex, toIndex);
    }
    /*
     * @see java.util.List#subList(int, int)
     */
    @Override
    public List<V> subList (int fromIndex, int toIndex)
    {
        return subList(_dataRows, fromIndex, toIndex);
    }

    private V[]    _emptyVals;
    @SuppressWarnings("unchecked")
    private V[] getEmptyValuesArray ()
    {
        if (null == _emptyVals)
            _emptyVals = (V[]) Array.newInstance(getValuesClass(), 0);
        return _emptyVals;
    }

    protected boolean isMatchingArray (Object[] a)
    {
        final Class<?>    ac=a.getClass().getComponentType();
        final Class<V>    vc=getValuesClass();
        return ac.isAssignableFrom(vc);
    }

    protected <T> T[] toArray (List<V> rows, T[] a)
    {
        if (!isMatchingArray(a))
            throw new ClassCastException("array type not assignable from value type");

        return rows.toArray(a);
    }
    /*
     * @see java.util.List#toArray(T[])
     */
    @Override
    public <T> T[] toArray (T[] a)
    {
        return toArray(_dataRows, a);
    }

    private static final Object[]    EMPTY_ARRAY=new Object[0];
    protected Object[] toArray (List<V> rows)
    {
        if (size() <= 0)
            return EMPTY_ARRAY;

        return toArray(rows, getEmptyValuesArray());
    }
    /*
     * @see java.util.List#toArray()
     */
    @Override
    public Object[] toArray ()
    {
        return toArray(_dataRows);
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final Class<?>    vClass=getValuesClass();
        return ((null == vClass) ? null : vClass.getName()) + "[" + getRowCount() + "][" + getColumnCount() + "]";
    }
}
