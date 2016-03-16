/*
 *
 */
package net.community.apps.apache.maven.pom2cpsync;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.table.TableCellRenderer;

import net.community.chest.CoVariantReturn;
import net.community.chest.apache.maven.helpers.DependenciesList;
import net.community.chest.ui.helpers.table.TypedTable;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2008 1:28:02 PM
 */
public class DependencyDetailsTable extends TypedTable<DependencyTargetEntry> {
    /**
     *
     */
    private static final long serialVersionUID = -4466027666470559750L;
    public DependencyDetailsTable (final DependencyDetailsTableModel model)
    {
        super(model);

        setRowSorter(new DependencyDetailsTableRowSorter(model));
    }

    public DependencyDetailsTable ()
    {
        this(new DependencyDetailsTableModel());
    }
    /*
     * @see javax.swing.JTable#getRowSorter()
     */
    @Override
    @CoVariantReturn
    public DependencyDetailsTableRowSorter getRowSorter ()
    {
        return DependencyDetailsTableRowSorter.class.cast(super.getRowSorter());
    }
    /*
     * @see net.community.chest.swing.component.table.TypedTable#getTypedModel()
     */
    @Override
    @CoVariantReturn
    public DependencyDetailsTableModel /* co-variant */ getTypedModel ()
    {
        return DependencyDetailsTableModel.class.cast(super.getTypedModel());
    }
    /**
     * Looks for 1st entry whose group/artifact/version matches the specified one
     * (case <U>insensitive</U>).
     * @param groupId Group ID
     * @param artifactId Artifact name
     * @param version Version name - if null/empty then version is ignored
     * @return Index of 1st match - negative if not found
     */
    public int indexOf (final String groupId, final String artifactId, final String version /* may be null/empty */)
    {
        return DependenciesList.indexOf(getTypedModel(), groupId, artifactId, version);
    }
    /**
     * Looks for 1st entry whose group/artifact matches the specified one
     * (case <U>insensitive</U>).
     * @param groupId Group ID
     * @param artifactId Artifact name
     * @return Index of 1st match - negative if not found
     * @see #indexOf(String, String, String) for specifying a version as well
     */
    public int indexOf (final String groupId, final String artifactId)
    {
        return indexOf(groupId, artifactId, null);
    }

    private Collection<? extends DependencyTargetEntry>    _lastDeps    /* =null */;
    private void setDependencies (final Collection<? extends DependencyTargetEntry> deps, final boolean saveDeps)
    {
        final DependencyDetailsTableModel    model=getTypedModel();
        final int                            curItems=model.size(), numDeps=(null == deps) ? 0 : deps.size();
        model.setDependencies(deps);

        if (saveDeps)
            _lastDeps = deps;

        // signal change only if had some items or have some new ones
        if ((curItems > 0) || (numDeps > 0))
            model.fireTableDataChanged();
    }

    public void setDependencies (final Collection<? extends DependencyTargetEntry> deps)
    {
        setDependencies(deps, true);
    }
    /* Since renderer(s) are re-used we need to prepare them every time
     * @see javax.swing.JTable#prepareRenderer(javax.swing.table.TableCellRenderer, int, int)
     */
    @Override
    public Component prepareRenderer (TableCellRenderer renderer, int row, int column)
    {
        final Component    c=super.prepareRenderer(renderer, row, column);
        if (null == c)
            return c;

        final DependencyDetailsTableModel    model=getTypedModel();
        final int                            numRows=(null == model) ? 0 : model.size();
        final DependencyTargetEntry         t=((row < 0) || (row >= numRows)) ? null : model.get(row);
        final Color                            cc=(null == t) ? null : t.getMatchColor();
        if (cc != null)
        {
            final Font    f=c.getFont(),
                        ef=(null == f) ? null : f.deriveFont(Font.BOLD);
            if (ef != null)
                c.setFont(ef);
            c.setBackground(cc);
        }
        else
            c.setBackground(getBackground());
        return c;
    }

    public void markMismatchedDependencies (final Collection<? extends Map.Entry<Integer,Color>> cl)
    {
        if ((null == cl) || (cl.size() <= 0))
            return;

        final DependencyDetailsTableModel    model=getTypedModel();
        final int                            numRows=(null == model) ? 0 : model.size();
        for (final Map.Entry<Integer,Color> ce : cl)
        {
            final Integer                rowIndex=(null == ce) ? null : ce.getKey();
            final int                    row=(null == rowIndex) ? Integer.MIN_VALUE : rowIndex.intValue();
            final Color                    c=(null == ce) ? null : ce.getValue();
            final DependencyTargetEntry t=((row < 0) || (row >= numRows)) ? null : model.get(row);
            if ((null == t) || (null == c))
                continue;

            t.setMatchColor(c);
        }

        if (model != null)
            model.fireTableDataChanged();
    }

    private DependencyMismatchType    _filterMode=DependencyMismatchType.ALL;
    public DependencyMismatchType getFilterMode ()
    {
        return _filterMode;
    }

    private static Collection<? extends DependencyTargetEntry> filterDependencies (
            final Collection<? extends DependencyTargetEntry>     deps,
            final DependencyMismatchType                        mode)
    {
        if ((null == mode) || DependencyMismatchType.ALL.equals(mode)
         || (null == deps) || (deps.size() <= 0))
            return deps;

        Collection<DependencyTargetEntry>    ret=null;
        for (final DependencyTargetEntry tgt : deps)
        {
            if (null == tgt)
                continue;

            final Color    c=tgt.getMatchColor();
            switch(mode)
            {
                case ALL        :
                    break;    // should not happen

                case MATCHING    :
                    if (c != null)
                        continue;
                    break;

                case MISMATCHED    :
                    if ((null == c) || (!DependencyTargetEntry.BAD_VERSION_COLOR.equals(c)))
                        continue;
                    break;

                case MISSING    :
                    if ((null == c) || (!DependencyTargetEntry.NO_ENTRY_COLOR.equals(c)))
                        continue;
                    break;

                default            :
                    // should not happen
            }

            if (null == ret)
                ret = new LinkedList<DependencyTargetEntry>();
            ret.add(tgt);
        }

        return ret;
    }
    // filtering is implemented here instead of the sorter since doing it in the sorter causes a row number mismatch with "prepareRenderer"
    public void setFilterMode (final DependencyMismatchType    mode)
    {
        if (null == (_filterMode=mode))
            _filterMode = DependencyMismatchType.ALL;

        final Collection<? extends DependencyTargetEntry>    deps=filterDependencies(_lastDeps, mode);
        setDependencies(deps, false);
    }
}
