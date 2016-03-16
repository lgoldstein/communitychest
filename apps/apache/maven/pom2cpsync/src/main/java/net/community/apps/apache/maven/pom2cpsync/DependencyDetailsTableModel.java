/**
 *
 */
package net.community.apps.apache.maven.pom2cpsync;

import java.util.Collection;
import java.util.NoSuchElementException;

import net.community.chest.CoVariantReturn;
import net.community.chest.apache.maven.helpers.DependenciesList;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2008 1:32:04 PM
 */
public class DependencyDetailsTableModel extends EnumColumnAbstractTableModel<DependencyDetailsColumns,DependencyTargetEntry> {
    /**
     *
     */
    private static final long serialVersionUID = -5526585123126268427L;
    public DependencyDetailsTableModel (int initialSize) throws IllegalArgumentException
    {
        super(DependencyDetailsColumns.class, DependencyTargetEntry.class, initialSize);
        setColumnsValues(DependencyDetailsColumns.VALUES);    // make sure using the cached instance

        // TODO use reflective proxy to configure table columns
        final DependencyDetailsTableColumn[]    cols={
                new DependencyDetailsTableColumn(DependencyDetailsColumns.GROUP, "Group", 45),
                new DependencyDetailsTableColumn(DependencyDetailsColumns.ARTIFACT, "Artifact", 35),
                new DependencyDetailsTableColumn(DependencyDetailsColumns.VERSION, "Version", 20)
            };
        // NOTE: columns must be added BEFORE model is attached to a table
        for (final DependencyDetailsTableColumn c : cols)
        {
            if (null == c)    // should not happen
                continue;

            addColumn(c);
        }
    }

    public DependencyDetailsTableModel () throws IllegalArgumentException
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
    public String getColumnValue (int rowIndex, DependencyTargetEntry row, DependencyDetailsColumns colIndex)
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
    /* NOTE: unexpected but allow it
     * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#setValueAt(int, java.lang.Object, int, java.lang.Enum, java.lang.Object)
     */
    @Override
    public void setValueAt (int rowIndex, DependencyTargetEntry row, int colNum, DependencyDetailsColumns colIndex, Object value)
    {
        if (null == colIndex)
            throw new NoSuchElementException(ClassUtil.getArgumentsExceptionLocation(getClass(), "setValueAt", Integer.valueOf(rowIndex), Integer.valueOf(colNum), colIndex, value) + " unresolved column");
        if (null == row)
            throw new IllegalStateException("setValueAt(" + rowIndex + "/" + colIndex + ") no row data");

        switch(colIndex)
        {
            case ARTIFACT    : row.setArtifactId((null == value) ? null : value.toString()); break;
            case GROUP        : row.setGroupId((null == value) ? null : value.toString()); break;
            case VERSION    : row.setVersion((null == value) ? null : value.toString()); break;
            default            :
                throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "setValueAt", Integer.valueOf(rowIndex), Integer.valueOf(colNum), colIndex, value) + " unexpected column");
        }
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
        return DependenciesList.indexOf(this, groupId, artifactId, version);
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
    // NOTE: must call "fireTableDataChanged" afterwards
    public void setDependencies (final Collection<? extends DependencyTargetEntry> deps)
    {
        final int    curDeps=size(), numDeps=(null == deps) ? 0 : deps.size();
        if (curDeps > 0)
            clear();

        if (numDeps > 0)
        {
            for (final DependencyTargetEntry d : deps)
            {
                if (null == d)
                    continue;
                add(d, false /* don't fire a "rowAddedEvent" since "fireTableDataChanged" is called afterwards */);
            }
        }
    }
}
