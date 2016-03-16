package net.community.chest.ui.components.dialog.manifest;

import net.community.chest.CoVariantReturn;
import net.community.chest.ui.helpers.table.TypedTable;
import net.community.chest.util.map.entries.StringPairEntry;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2007 4:09:15 PM
 */
public class ManifestTable extends TypedTable<StringPairEntry> {
    /**
     *
     */
    private static final long serialVersionUID = -2323163093054098970L;
    public ManifestTable (final ManifestTableModel model)
    {
        super(model);
    }

    public ManifestTable ()
    {
        this(new ManifestTableModel());
    }
    /*
     * @see javax.swing.JTable#getCellRenderer(int, int)
     */
    @Override
    @CoVariantReturn
    public ManifestTableCellRenderer getCellRenderer (int row, int column)
    {
        return new ManifestTableCellRenderer();
    }
}
