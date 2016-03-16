/*
 *
 */
package net.community.chest.ui.components.table.file;

import java.io.File;
import java.util.Date;
import java.util.NoSuchElementException;


/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 4, 2009 2:22:57 PM
 */
public class DefaultFilesTableModel
            extends AbstractFilesTableModel<FilesTableColumns,File> {
    /**
     *
     */
    private static final long serialVersionUID = 5131283730479244998L;
    public DefaultFilesTableModel ()
    {
        super(FilesTableColumns.class, File.class);
        setColumnsValues(FilesTableColumns.VALUES);
    }
    /*
     * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#getColumnValue(int, java.lang.Object, java.lang.Enum)
     */
    @Override
    public Object getColumnValue (int rowIndex, File row, FilesTableColumns colIndex)
    {
        if (null == colIndex)
            throw new IllegalStateException("getColumnValue(" + rowIndex + ") no column");
        if (null == row)
            throw new IllegalStateException("getColumnValue(" + rowIndex + "/" + colIndex + ") no row data");

        switch(colIndex)
        {
            case NAME        :
                return row.getName();
            case MODTIME    :
                return new Date(row.lastModified());
            case SIZE        :
                return Long.valueOf(row.length());
            case TYPE        :
                return row;
            case ATTRS        :
                return FileAttrsCellRenderer.getFileAttributes(row);

            default            :
                throw new NoSuchElementException("getColumnValue(" + rowIndex + "/" + colIndex + ") unknown column requested");
        }
    }
    /*
     * @see net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel#setValueAt(int, java.lang.Object, int, java.lang.Enum, java.lang.Object)
     */
    @Override
    public void setValueAt (int rowIndex, File row, int colNum, FilesTableColumns colIndex, Object value)
    {
        throw new UnsupportedOperationException("setValueAt(" + row + ":" + colNum + ")[" + colIndex + "]@" + rowIndex + "=" + value + " - N/A");
    }
}
