/*
 *
 */
package net.community.chest.ui.helpers.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <E> Type of {@link Enum} used as column identifier
 * @author Lyor G.
 * @since Aug 6, 2009 8:26:47 AM
 */
public class EnumTableColumnModel<E extends Enum<E>>
                extends DefaultTableColumnModel
                implements TypedValuesContainer<E>,
                           Map<E,EnumTableColumn<E>> {
    /**
     *
     */
    private static final long serialVersionUID = 5456928800811503251L;
    private final Class<E>    _ec;
    /*
     * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final Class<E> getValuesClass ()
    {
        return _ec;
    }

    public EnumTableColumnModel (Class<E> ec)
    {
        if (null == (_ec=ec))
            throw new IllegalArgumentException("No " + Enum.class.getSimpleName() + " class provided");
    }

    public void addColumn (final EnumTableColumn<E>    col)
    {
        super.addColumn(col);
    }
    /*
     * @see javax.swing.table.DefaultTableColumnModel#addColumn(javax.swing.table.TableColumn)
     */
    @Override
    public void addColumn (TableColumn aColumn)
    {
        if (!(aColumn instanceof EnumTableColumn<?>))
            throw new IllegalArgumentException("addColumn(" + aColumn + ") not an " + EnumTableColumn.class.getSimpleName());

        @SuppressWarnings("unchecked")
        final EnumTableColumn<E>    col=(EnumTableColumn<E>) aColumn;
        addColumn(col);
    }
    /*
     * @see javax.swing.table.DefaultTableColumnModel#getColumn(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    @CoVariantReturn
    public EnumTableColumn<E> getColumn (int columnIndex)
    {
        return (EnumTableColumn<E>) super.getColumn(columnIndex);
    }
    /*
     * @see javax.swing.table.DefaultTableColumnModel#getColumnIndex(java.lang.Object)
     */
    @Override
    public int getColumnIndex (Object identifier)
    {
        final Class<?>    ic=(null == identifier) ? null : identifier.getClass(),
                        ec=getValuesClass();
        if ((null == ic) || (!ec.isAssignableFrom(ic)))
            throw new IllegalArgumentException("getColumnIndex(" + identifier + ") bad class - expected=" + ec.getName() + "/got=" + ((null == ic) ? null : ic.getName()));

        return super.getColumnIndex(identifier);
    }

    public void removeColumn (EnumTableColumn<E> column)
    {
        super.removeColumn(column);
    }
    /*
     * @see javax.swing.table.DefaultTableColumnModel#removeColumn(javax.swing.table.TableColumn)
     */
    @Override
    public void removeColumn (TableColumn column)
    {
        if (!(column instanceof EnumTableColumn<?>))
            throw new IllegalArgumentException("addColumn(" + column + ") not an " + EnumTableColumn.class.getSimpleName());

        @SuppressWarnings("unchecked")
        final EnumTableColumn<E>    col=(EnumTableColumn<E>) column;
        removeColumn(col);
    }
    /*
     * @see java.util.Map#size()
     */
    @Override
    public int size ()
    {
        return getColumnCount();
    }
    /*
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty ()
    {
        return (size() <= 0);
    }
    /*
     * @see java.util.Map#clear()
     */
    @Override
    public void clear ()
    {
        if (size() <= 0)
            return;

        for (final Enumeration<? extends TableColumn>    cols=getColumns();
             (cols != null) && cols.hasMoreElements();
             )
        {
            final TableColumn    tc=cols.nextElement();
            if (null == tc)
                continue;
            removeColumn(tc);
        }
    }
    /*
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public EnumTableColumn<E> get (final Object key)
    {
        final Class<?>    kc=(null == key) ? null : key.getClass();
        final Class<E>    ec=getValuesClass();
        if ((null == kc) || (!ec.isAssignableFrom(kc)))
            return null;

        if (size() <= 0)
            return null;

        final E    kv=ec.cast(key);
        for (final Enumeration<? extends TableColumn>    cols=getColumns();
             (cols != null) && cols.hasMoreElements();
             )
        {
            final TableColumn    tc=cols.nextElement();
            if (!(tc instanceof EnumTableColumn<?>))
                continue;

            @SuppressWarnings("unchecked")
            final EnumTableColumn<E>    col=(EnumTableColumn<E>) tc;
            final E                        ev=col.getColumnValue();
            if (kv.equals(ev))
                return col;
        }

        return null;
    }
    /*
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey (Object key)
    {
        return get(key) != null;
    }
    /*
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue (Object value)
    {
        if (!(value instanceof EnumTableColumn<?>))
            return false;

        for (final Enumeration<? extends TableColumn>    cols=getColumns();
              (cols != null) && cols.hasMoreElements();
             )
        {
            final TableColumn    tc=cols.nextElement();
            if ((tc == value) || ((tc != null) && tc.equals(value)))
                return true;
        }

        return false;
    }
    /*
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public EnumTableColumn<E> put (E key, EnumTableColumn<E> value)
    {
        if ((null == key) || (null == value))
            throw new IllegalArgumentException("put(" + key + ")[" + value + "] null(s) not allowed");

        final EnumTableColumn<E>    prev=get(key);
        if (prev != null)
            removeColumn(prev);
        addColumn(value);
        return prev;
    }
    /*
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll (Map<? extends E,? extends EnumTableColumn<E>> m)
    {
        final Collection<? extends Map.Entry<? extends E,? extends EnumTableColumn<E>>>    ml=
            ((null == m) || (m.size() <= 0)) ? null : m.entrySet();
        if ((null == ml) || (ml.size() <= 0))
            return;

        for (final Map.Entry<? extends E,? extends EnumTableColumn<E>> ce : ml)
        {
            if (null == ce)
                return;
            put(ce.getKey(), ce.getValue());
        }
    }
    /*
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public EnumTableColumn<E> remove (Object key)
    {
        final EnumTableColumn<E>    prev=get(key);
        if (prev != null)
            removeColumn(prev);
        return prev;
    }
    /*
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<E> keySet ()
    {
        final Class<E>    ec=getValuesClass();
        final Set<E>    ks=EnumSet.noneOf(ec);
        final E[]        ka=ec.getEnumConstants();
        for (final E k : ka)
        {
            if (!containsKey(k))
                continue;
            ks.add(k);
        }

        return ks;
    }
    /*
     * @see java.util.Map#values()
     */
    @Override
    public Collection<EnumTableColumn<E>> values ()
    {
        final int    numVals=size();
        if (numVals <= 0)
            return Collections.emptyList();

        final Collection<EnumTableColumn<E>>    ret=new ArrayList<EnumTableColumn<E>>(numVals);
        for (final Enumeration<? extends TableColumn>    cols=getColumns();
               (cols != null) && cols.hasMoreElements();
            )
        {
            final TableColumn            tc=cols.nextElement();
            @SuppressWarnings("unchecked")
            final EnumTableColumn<E>    col=(EnumTableColumn<E>) tc;
            ret.add(col);
        }

        return ret;
    }
    /*
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<Map.Entry<E,EnumTableColumn<E>>> entrySet ()
    {
        final int    numVals=size();
        if (numVals <= 0)
            return Collections.emptySet();

        final Set<Map.Entry<E,EnumTableColumn<E>>>    ret=
            new HashSet<Map.Entry<E,EnumTableColumn<E>>>(numVals);
        for (final Enumeration<? extends TableColumn>    cols=getColumns();
             (cols != null) && cols.hasMoreElements();
            )
        {
            final TableColumn            tc=cols.nextElement();
            @SuppressWarnings("unchecked")
            final EnumTableColumn<E>    col=(EnumTableColumn<E>) tc;
            final E                        kv=col.getColumnValue();
            ret.add(new MapEntryImpl<E,EnumTableColumn<E>>(kv, col));
        }

        return ret;
    }
}
