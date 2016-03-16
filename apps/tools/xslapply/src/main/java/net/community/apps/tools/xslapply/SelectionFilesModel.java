/*
 *
 */
package net.community.apps.tools.xslapply;

import net.community.chest.CoVariantReturn;
import net.community.chest.ui.helpers.table.AbstractTypedTableModel;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 11, 2008 11:49:54 AM
 */
public class SelectionFilesModel extends AbstractTypedTableModel<SelectionFilePair> {
    /**
     *
     */
    private static final long serialVersionUID = 2109372295533509876L;
    public SelectionFilesModel ()
    {
        super(SelectionFilePair.class);
    }
    /*
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount ()
    {
        return 1;
    }
    /*
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName (int column)
    {
        if (column != 0)
            return null;

        return "XSL Files";
    }
    /*
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    @Override
    public Class<?> getColumnClass (int columnIndex)
    {
        if (columnIndex != 0)
            return Object.class;

        return SelectionFilePair.class;
    }
    /*
     * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#getColumnValue(int, java.lang.Object, int)
     */
    @Override
    @CoVariantReturn
    public SelectionFilePair getColumnValue (int rowIndex, SelectionFilePair row, int colIndex)
    {
        if (colIndex != 0)
            return null;

        return row;
    }
    /*
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable (int rowIndex, int columnIndex)
    {
        return (0 == columnIndex);
    }
    /*
     * @see net.community.chest.ui.helpers.table.AbstractTypedTableModel#setValueAt(int, java.lang.Object, int, java.lang.Object)
     */
    @Override
    public void setValueAt (int rowIndex, SelectionFilePair row, int colIndex, Object value)
    {
        // ignored since the SelectionFilePair(s) are shared instances
    }
}
