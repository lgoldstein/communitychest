package net.community.apps.common.test.gridbag;

import java.util.List;

import net.community.chest.awt.layout.gridbag.GridBagFillType;
import net.community.chest.ui.helpers.combobox.EnumComboBox;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 19, 2008 1:37:37 PM
 */
public class GridBagFillsChoice extends EnumComboBox<GridBagFillType> {
    /**
     *
     */
    private static final long serialVersionUID = -2475756270161342899L;

    /*
     * @see net.community.chest.ui.helpers.combobox.EnumComboBox#getEnumValues()
     */
    @Override
    public synchronized List<GridBagFillType> getEnumValues ()
    {
        return GridBagFillType.VALUES;
    }

    public GridBagFillsChoice (boolean autoPopulate)
    {
        super(GridBagFillType.class, autoPopulate);
    }

    public GridBagFillsChoice ()
    {
        this(false);
    }
}
