/*
 *
 */
package net.community.apps.eclipse.cp2pom;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 27, 2009 10:24:41 AM
 */
public class RepositoryEntriesModel extends EnumColumnAbstractTableModel<RepositoryEntryColumns,RepositoryEntry> {
    /**
     *
     */
    private static final long serialVersionUID = -7032428636516679290L;

    public RepositoryEntriesModel (final int initialSize)
    {
        super(RepositoryEntryColumns.class, RepositoryEntry.class, initialSize);
        setColumnsValues(RepositoryEntryColumns.VALUES);    // make sure using the cached instance

        // TODO use reflective proxy to configure table columns
        final RepositoryEntryTableColumn[]    cols={
                new RepositoryEntryTableColumn(RepositoryEntryColumns.GROUP, "Group", 45),
                new RepositoryEntryTableColumn(RepositoryEntryColumns.ARTIFACT, "Artifact", 35),
                new RepositoryEntryTableColumn(RepositoryEntryColumns.VERSION, "Version", 20)
            };
        // NOTE: columns must be added BEFORE model is attached to a table
        for (final RepositoryEntryTableColumn c : cols)
        {
            if (null == c)    // should not happen
                continue;

            addColumn(c);
        }
    }

    public RepositoryEntriesModel ()
    {
        this(0);
    }
    /*
     * @see net.community.chest.swing.component.table.EnumColumnAbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass (int columnIndex)
    {
        return String.class;    // all our values are strings
    }
    /*
     * @see net.community.chest.swing.component.table.EnumColumnAbstractTableModel#getColumnValue(int, java.lang.Object, java.lang.Enum)
     */
    @Override
    @CoVariantReturn
    public String getColumnValue (int rowIndex, RepositoryEntry row, RepositoryEntryColumns colIndex)
    {
        if (null == colIndex)
            throw new NoSuchElementException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getColumnValue", Integer.valueOf(rowIndex), colIndex) + " unresolved column");
        if (null == row)
            throw new IllegalStateException("getColumnValue(" + rowIndex + "/" + colIndex + ") no row data");

        switch(colIndex)
        {
            case ARTIFACT    : return row.getArtifactId();
            case GROUP        : return row.getGroupId();
            case VERSION    : return row.getVersion();
            default            :
                throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getColumnValue", Integer.valueOf(rowIndex), colIndex) + " unexpected column");
        }
    }
    /*
     * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#setValueAt(int, java.lang.Object, int, java.lang.Enum, java.lang.Object)
     */
    @Override
    public void setValueAt (int rowIndex, RepositoryEntry row, int colNum, RepositoryEntryColumns colIndex, Object value)
    {
        if (null == colIndex)
            throw new NoSuchElementException(ClassUtil.getArgumentsExceptionLocation(getClass(), "setValueAt", Integer.valueOf(rowIndex), Integer.valueOf(colNum), colIndex, value) + " unresolved column");
        if (null == row)
            throw new IllegalStateException("setValueAt(" + rowIndex + ":" + colNum + "/" + colIndex + ") no row data");

        final String    curValue=getColumnValue(rowIndex, row, colIndex),
                        newValue=(null == value) ? null : value.toString();
        if (0 == StringUtil.compareDataStrings(curValue, newValue, true))
            return;    // ignore if nothing changed

        switch(colIndex)
        {
            case ARTIFACT    : row.setArtifactId(newValue); break;
            case GROUP        : row.setGroupId(newValue); break;
            case VERSION    : row.setVersion(newValue); break;
            default            :
                throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "setValueAt", Integer.valueOf(rowIndex), Integer.valueOf(colNum), colIndex, value) + " unexpected column");
        }
    }
    /*
     * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#isCellEditable(int, java.lang.Object, int, java.lang.Enum)
     */
    @Override
    public boolean isCellEditable (int rowIndex, RepositoryEntry row, int colNum, RepositoryEntryColumns colIndex)
    {
        return (row != null)
             && (colIndex != null)
            && (rowIndex >= 0) && (rowIndex < size())
            && (colNum >= 0) && (colNum < getColumnCount())
            ;
    }

    public List<? extends RepositoryEntry> getEntries ()
    {
        return this;
    }

    public void setEntries (final Collection<? extends RepositoryEntry> rl)
    {
        final int    curDeps=size(), numDeps=(null == rl) ? 0 : rl.size();
        if (curDeps > 0)
            clear();

        if (numDeps > 0)
        {
            for (final RepositoryEntry re : rl)
            {
                if (null == re)
                    continue;
                add(re, false /* don't fire a "rowAddedEvent" since "fireTableDataChanged" is called afterwards */);
            }
        }
    }
}
