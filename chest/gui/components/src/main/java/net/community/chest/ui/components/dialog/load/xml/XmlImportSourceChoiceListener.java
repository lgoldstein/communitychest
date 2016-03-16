/*
 *
 */
package net.community.chest.ui.components.dialog.load.xml;

import net.community.chest.ui.helpers.combobox.EnumComboBox;
import net.community.chest.ui.helpers.combobox.TypedComboBoxActionListener;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 31, 2009 12:41:00 PM
 */
public abstract class XmlImportSourceChoiceListener extends
        TypedComboBoxActionListener<XmlImportSource,EnumComboBox<XmlImportSource>> {
    protected XmlImportSourceChoiceListener ()
    {
        super();
    }
}
