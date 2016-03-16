/*
 *
 */
package net.community.chest.swing.component.table;

import javax.swing.JMenu;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 17, 2008 2:44:43 PM
 */
public class BaseTableColumn extends TableColumn implements XmlConvertible<TableColumn> {
    /**
     *
     */
    private static final long serialVersionUID = -7160607858849883569L;
    public BaseTableColumn (int index, int w, TableCellRenderer cr, TableCellEditor ce)
    {
        super(index, w, cr, ce);
    }

    public BaseTableColumn (int index, int w)
    {
        this(index, w, null, null);
    }

    public static final int    DEFAULT_WIDTH=25;
    public BaseTableColumn (int index)
    {
        this(index, DEFAULT_WIDTH);
    }

    public BaseTableColumn ()
    {
        this(0);
    }

    protected XmlProxyConvertible<?> getColumnConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : TableColumnReflectiveProxy.TBLCOL;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseTableColumn fromXml (Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getColumnConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    co=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (co != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + " mismatched initialization instances");

        return this;
    }

    public BaseTableColumn (Element elem) throws Exception
    {
        if (fromXml(elem) != this)
            throw new IllegalStateException("<init>" + DOMUtils.toString(elem) + ") mismatched restored " + JMenu.class.getName() + " instances");
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
    }

    public String getColumnName ()
    {
        final Object    hv=getHeaderValue();
        return (null == hv) ? null : hv.toString();
    }

    public void setColumnName (String n)
    {
        setHeaderValue(n);
    }
}
