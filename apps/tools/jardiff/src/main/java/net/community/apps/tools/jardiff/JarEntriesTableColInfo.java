/*
 *
 */
package net.community.apps.tools.jardiff;

import org.w3c.dom.Element;

import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.ui.helpers.table.EnumTableColumn;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 2, 2011 12:09:23 PM
 */
public class JarEntriesTableColInfo extends EnumTableColumn<JarEntriesTableColumns> {
    /**
     *
     */
    private static final long serialVersionUID = -2502890650914408359L;
    public JarEntriesTableColInfo (JarEntriesTableColumns colIndex, String colName, int colWidth)
    {
        super(JarEntriesTableColumns.class, colIndex, colWidth);
        setColumnName(colName);
    }

    public JarEntriesTableColInfo (Element elem) throws Exception
    {
        super(JarEntriesTableColumns.class, elem);
    }
    /*
     * @see net.community.chest.swing.component.table.BaseTableColumn#getColumnConverter(org.w3c.dom.Element)
     */
    @Override
    protected XmlProxyConvertible<?> getColumnConverter (Element elem) throws Exception
    {
        return (elem == null) ? null : JarEntriesTableColumnReflectiveProxy.DEFAULT;
    }
}
