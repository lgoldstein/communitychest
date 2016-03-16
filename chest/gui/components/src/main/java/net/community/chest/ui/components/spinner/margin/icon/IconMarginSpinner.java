/*
 *
 */
package net.community.chest.ui.components.spinner.margin.icon;

import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.ui.components.spinner.margin.MarginSpinner;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 12, 2009 10:04:56 AM
 */
public class IconMarginSpinner extends MarginSpinner {
    /**
     *
     */
    private static final long serialVersionUID = -8409049044902975267L;

    public IconMarginSpinner (BorderLayoutPosition p)
    {
        super(new IconMarginSpinnerModel(), p);
    }
}
