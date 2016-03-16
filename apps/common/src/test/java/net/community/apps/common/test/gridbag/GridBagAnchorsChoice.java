package net.community.apps.common.test.gridbag;

import java.util.List;

import net.community.chest.awt.layout.gridbag.GridBagAnchorType;
import net.community.chest.ui.helpers.combobox.EnumComboBox;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 19, 2008 1:33:42 PM
 */
public class GridBagAnchorsChoice extends EnumComboBox<GridBagAnchorType> {
    /**
     *
     */
    private static final long serialVersionUID = -1159194208906359288L;

    /*
     * @see net.community.chest.ui.helpers.combobox.EnumComboBox#getEnumValues()
     */
    @Override
    public synchronized List<GridBagAnchorType> getEnumValues ()
    {
        return GridBagAnchorType.VALUES;
    }

    public GridBagAnchorsChoice (boolean autoPopulate)
    {
        super(GridBagAnchorType.class, autoPopulate);
    }

    public GridBagAnchorsChoice ()
    {
        this(false);
    }
}
