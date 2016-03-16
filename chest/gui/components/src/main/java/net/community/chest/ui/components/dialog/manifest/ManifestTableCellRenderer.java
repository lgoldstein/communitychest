package net.community.chest.ui.components.dialog.manifest;

import java.awt.Color;

import net.community.chest.ui.helpers.table.AlternateRowColorCellRenderer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 25, 2008 12:49:00 PM
 */
public class ManifestTableCellRenderer extends AlternateRowColorCellRenderer {
    /**
     *
     */
    private static final long serialVersionUID = 421444376348240878L;
    public static final Color DFLT_ROWCOLOR=Color.RED, DFLT_ALTROWCOLOR=Color.YELLOW;

    public ManifestTableCellRenderer ()
    {
        super(DFLT_ROWCOLOR, DFLT_ALTROWCOLOR);
    }

    public ManifestTableCellRenderer (Color... colors)
    {
        super(colors);
    }

    public ManifestTableCellRenderer (Iterable<? extends Color> colors)
    {
        super(colors);
    }
}
