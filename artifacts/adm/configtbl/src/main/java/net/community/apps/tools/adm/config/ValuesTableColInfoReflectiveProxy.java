/*
 *
 */
package net.community.apps.tools.adm.config;

import net.community.chest.ui.helpers.table.EnumTableColumnReflectiveProxy;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <C> Type of {@link ValuesTableColInfo} being reflected
 * @author Lyor G.
 * @since Oct 15, 2009 10:51:47 AM
 */
public class ValuesTableColInfoReflectiveProxy<C extends ValuesTableColInfo>
        extends EnumTableColumnReflectiveProxy<ValuesTableColumn,C> {
    protected ValuesTableColInfoReflectiveProxy (Class<C> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(ValuesTableColumn.class, objClass, registerAsDefault);
    }

    public ValuesTableColInfoReflectiveProxy (Class<C> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final ValuesTableColInfoReflectiveProxy<ValuesTableColInfo>    VALSTBLCOLS=
        new ValuesTableColInfoReflectiveProxy<ValuesTableColInfo>(ValuesTableColInfo.class);

}
