/*
 *
 */
package net.community.chest.ui.components.dialog.load.xml;

import org.w3c.dom.Element;

import net.community.chest.ui.helpers.combobox.EnumComboBox;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 31, 2009 12:33:39 PM
 */
public class XmlImportSourceChoice extends EnumComboBox<XmlImportSource> {
    /**
     *
     */
    private static final long serialVersionUID = 1098730859948647611L;

    public XmlImportSourceChoice (boolean autoPopulate, Element elem, boolean autoLayout)
    {
        super(XmlImportSource.class, false /* delay auto-populate */, elem, false /* delay auto-layout */);

        setEnumValues(XmlImportSource.VALUES);

        if (autoLayout)
            layoutComponent();
        if (autoPopulate)
            populate();
    }

    public XmlImportSourceChoice (boolean autoPopulate, Element elem)
    {
        this(autoPopulate, elem, true);
    }

    public XmlImportSourceChoice (boolean autoPopulate)
    {
        this(autoPopulate, null);
    }

    public XmlImportSourceChoice ()
    {
        this(true);
    }
}
