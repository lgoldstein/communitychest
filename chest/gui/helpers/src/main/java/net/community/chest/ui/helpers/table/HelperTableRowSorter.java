/*
 *
 */
package net.community.chest.ui.helpers.table;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <M> Type of {@link TableModel} being used
 * @author Lyor G.
 * @since Aug 6, 2009 10:00:33 AM
 */
public class HelperTableRowSorter<M extends TableModel> extends TableRowSorter<M> {
    public HelperTableRowSorter (M model)
    {
        super(model);
    }

    public HelperTableRowSorter ()
    {
        this(null);
    }

    private Map<Enum<?>,Comparator<?>>    _compsMap;
    @SuppressWarnings({ "unchecked", "cast", "rawtypes" })
    protected <E extends Enum<E>> Map<E,Comparator<?>> getColComparatorsMap (
            final Class<E> colClass, final boolean createIfNotExist)
    {
        if ((null == _compsMap) && createIfNotExist)
            _compsMap = new EnumMap(colClass);
        return (Map<E,Comparator<?>>) ((Map) _compsMap);
    }

    protected <E extends Enum<E>> Map<E,Comparator<?>> getColComparatorsMap (final Class<E> colClass)
    {
        return getColComparatorsMap(colClass, false);
    }

    @SuppressWarnings({ "unchecked", "cast", "rawtypes" })
    protected <E extends Enum<E>> void setColComparatorsMap (Map<E,Comparator<?>> m)
    {
        _compsMap = (Map<Enum<?>,Comparator<?>>) ((Map) m);
    }

    public <E extends Enum<E>> Comparator<?> getColComparator (E colIndex)
    {
        @SuppressWarnings("unchecked")
        final Map<E,? extends Comparator<?>>    m=
            (null == colIndex) ? null : getColComparatorsMap(colIndex.getClass());
        if ((null == m) || (m.size() <= 0))
            return null;

        return m.get(colIndex);
    }
    // returns previous instance (if any)
    public <E extends Enum<E>> Comparator<?> setColComparator (
            final E colIndex, final Comparator<?> c /* null == remove */)
    {
        if (null == colIndex)
            return null;

        @SuppressWarnings("unchecked")
        final Map<E,Comparator<?>>    m=
            getColComparatorsMap(colIndex.getClass(), c != null);
        if (null == c)    // asked to remove
        {
            if ((null == m) || (m.size() <= 0))
                return null;
            return m.remove(c);
        }

        return m.put(colIndex, c);
    }

    public <E extends Enum<E>> Comparator<?> getComparator (
            final int column, final E colIndex)
    {
        final Comparator<?>    c=getColComparator(colIndex);
        if (c != null)
            return c;

        return super.getComparator(column);
    }
}
