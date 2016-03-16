/*
 *
 */
package net.community.apps.tools.jgit.browser.reflog;

import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.ui.helpers.table.EnumTableColumn;

import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 20, 2011 11:56:04 AM
 */
public class ReflogEntryColInfo extends EnumTableColumn<ReflogEntryColumns> {
    /**
     *
     */
    private static final long serialVersionUID = -7661850657041521117L;
    public ReflogEntryColInfo (ReflogEntryColumns colIndex, String colName, int colWidth)
    {
        super(ReflogEntryColumns.class, colIndex, colWidth);
        setColumnName(colName);
    }

    public ReflogEntryColInfo (Element elem) throws Exception
    {
        super(ReflogEntryColumns.class, elem);
    }
    /*
     * @see net.community.chest.swing.component.table.BaseTableColumn#getColumnConverter(org.w3c.dom.Element)
     */
    @Override
    protected XmlProxyConvertible<?> getColumnConverter (Element elem) throws Exception
    {
        return (elem == null) ? null : ReflogEntryColumnReflectiveProxy.DEFAULT;
    }
}
