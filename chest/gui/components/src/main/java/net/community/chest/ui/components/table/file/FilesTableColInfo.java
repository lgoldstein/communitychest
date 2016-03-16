/*
 *
 */
package net.community.chest.ui.components.table.file;

import org.w3c.dom.Element;

import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.ui.helpers.table.EnumTableColumn;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 4, 2009 2:17:14 PM
 */
public class FilesTableColInfo extends EnumTableColumn<FilesTableColumns> {
    /**
     *
     */
    private static final long serialVersionUID = -6899106720714860019L;
    public FilesTableColInfo (FilesTableColumns colIndex, String colName, int colWidth)
    {
        super(FilesTableColumns.class, colIndex, colWidth);
        setColumnName(colName);
    }

    public FilesTableColInfo (FilesTableColumns colIndex, String colName)
    {
        this(colIndex, colName, DEFAULT_WIDTH);
    }

    public FilesTableColInfo (FilesTableColumns colIndex)
    {
        this(colIndex, (null == colIndex) ? null : colIndex.toString());
    }

    public FilesTableColInfo (Element elem) throws Exception
    {
        super(FilesTableColumns.class, elem);
    }
    /*
     * @see net.community.chest.swing.component.table.BaseTableColumn#getColumnConverter(org.w3c.dom.Element)
     */
    @Override
    protected XmlProxyConvertible<?> getColumnConverter (Element elem)
            throws Exception
    {
        return (null == elem) ? null : FilesTableColInfoReflectiveProxy.FLTBLCOLS;
    }
}
