/*
 *
 */
package net.community.apps.tools.adm.config;

import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.ui.helpers.table.EnumTableColumn;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 15, 2009 10:50:38 AM
 */
public class ValuesTableColInfo extends EnumTableColumn<ValuesTableColumn> {
    public ValuesTableColInfo (ValuesTableColumn colIndex, String colName, int colWidth)
    {
        super(ValuesTableColumn.class, colIndex, colWidth);
        setColumnName(colName);
    }

    public ValuesTableColInfo (ValuesTableColumn colIndex, String colName)
    {
        this(colIndex, colName, DEFAULT_WIDTH);
    }

    public ValuesTableColInfo (ValuesTableColumn colIndex)
    {
        this(colIndex, (null == colIndex) ? null : colIndex.toString());
    }

    public ValuesTableColInfo (Element elem) throws Exception
    {
        super(ValuesTableColumn.class, elem);
    }
    /*
     * @see net.community.chest.swing.component.table.BaseTableColumn#getColumnConverter(org.w3c.dom.Element)
     */
    @Override
    protected XmlProxyConvertible<?> getColumnConverter (Element elem)
            throws Exception
    {
        return (null == elem) ? null : ValuesTableColInfoReflectiveProxy.VALSTBLCOLS;
    }
}
