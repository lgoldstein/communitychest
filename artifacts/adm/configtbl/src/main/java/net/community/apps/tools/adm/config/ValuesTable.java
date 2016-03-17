/*
 *
 */
package net.community.apps.tools.adm.config;

import javax.swing.table.TableColumnModel;

import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;
import net.community.chest.ui.helpers.table.EnumColumnTypedTable;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 15, 2009 10:55:13 AM
 */
public class ValuesTable extends
        EnumColumnTypedTable<ValuesTableColumn,ValueTableEntry> {
    public ValuesTable (EnumColumnAbstractTableModel<ValuesTableColumn,ValueTableEntry> tbModel, TableColumnModel tcModel)
    {
        super(tbModel, tcModel);
    }

    public ValuesTable (EnumColumnAbstractTableModel<ValuesTableColumn,ValueTableEntry> model)
    {
        super(model, (null == model) ? null : model.getTableColumnModel());
    }

    public ValuesTable ()
    {
        this(new ValuesTableModel());
    }
}
