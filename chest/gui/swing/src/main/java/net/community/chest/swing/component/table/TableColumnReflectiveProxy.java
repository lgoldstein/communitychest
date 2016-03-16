/*
 *
 */
package net.community.chest.swing.component.table;

import java.util.Map;

import javax.swing.table.TableColumn;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <C> The reflected {@link TableColumn} instance
 * @author Lyor G.
 * @since Sep 17, 2008 2:42:56 PM
 */
public class TableColumnReflectiveProxy<C extends TableColumn> extends UIReflectiveAttributesProxy<C> {
    public TableColumnReflectiveProxy (Class<C> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected TableColumnReflectiveProxy (Class<C> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final String    HDRVALUE_ATTR="headerValue";
    /*
     * @see net.community.chest.dom.proxy.AbstractXmlProxyConverter#initializeAliasesMap(java.util.Map)
     */
    @Override
    protected Map<String,String> initializeAliasesMap (Map<String,String> org)
    {
        return addAttributeAliases(super.initializeAliasesMap(org),
                    NAME_ATTR, HDRVALUE_ATTR
                );
    }

    public static final TableColumnReflectiveProxy<TableColumn>    TBLCOL=
        new TableColumnReflectiveProxy<TableColumn>(TableColumn.class, true);
}
