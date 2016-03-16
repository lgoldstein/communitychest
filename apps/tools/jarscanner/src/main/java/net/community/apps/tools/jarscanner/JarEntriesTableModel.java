package net.community.apps.tools.jarscanner;

import java.util.NoSuchElementException;
import java.util.jar.JarEntry;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.jar.JarEntryLocation;
import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 22, 2007 10:56:09 AM
 */
public class JarEntriesTableModel extends EnumColumnAbstractTableModel<JarEntriesTableColumns,JarEntryLocation> {
    /**
     *
     */
    private static final long serialVersionUID = 172620071135119108L;
    public JarEntriesTableModel ()
    {
        super(JarEntriesTableColumns.class, JarEntryLocation.class);
        setColumnsValues(JarEntriesTableColumns.VALUES);
    }
    /*
     * @see net.community.chest.swing.component.table.EnumColumnAbstractTableModel#getColumnValue(int, java.lang.Object, java.lang.Enum)
     */
    @Override
    @CoVariantReturn
    public String getColumnValue (int rowIndex, JarEntryLocation row, JarEntriesTableColumns colIndex)
    {
        if (null == colIndex)
            throw new IllegalStateException("getColumnValue(" + rowIndex + ") no column");
        if (null == row)
            throw new IllegalStateException("getColumnValue(" + rowIndex + "/" + colIndex + ") no row data");

        switch(colIndex)
        {
            case JAR_PATH    :
                return row.getKey();

            case ENTRY_PATH    :
            case ENTRY_NAME    :
                {
                    final JarEntry    je=row.getValue();
                    final String    name=(null == je) /* should not happen */ ? null : je.getName();
                    final int        nLen=(null == name) ? 0 : name.length(),
                                    sPos=(nLen <= 1) ? (-1) : name.lastIndexOf('/');
                    if (JarEntriesTableColumns.ENTRY_NAME.equals(colIndex))
                    {
                        if ((sPos > 0) && (sPos < (nLen -1)))
                            return name.substring(sPos + 1);
                    }
                    else
                    {
                        if (sPos > 0)
                            return name.substring(0, sPos);
                    }

                    return name;
                }

            default            :
                throw new NoSuchElementException("getColumnValue(" + rowIndex + "/" + colIndex + ") unknown column requested");
        }
    }
    /*
     * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#setValueAt(int, java.lang.Object, int, java.lang.Enum, java.lang.Object)
     */
    @Override
    public void setValueAt (int rowIndex, JarEntryLocation row, int colNum, JarEntriesTableColumns colIndex, Object value)
    {
        throw new UnsupportedOperationException("setValueAt(" + rowIndex + ":" + colNum + "/" + colIndex + ")::=" + value + " - N/A");
    }
}
