package net.community.chest.ui.components.dialog.manifest;

import net.community.chest.swing.component.dialog.BaseDialogReflectiveProxy;
import net.community.chest.ui.helpers.table.EnumColumnAbstractTableModel;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> The reflected {@link ManifestDialog} type
 * @author Lyor G.
 * @since Mar 25, 2008 10:46:33 AM
 */
public class ManifestDialogReflectiveProxy<D extends ManifestDialog> extends BaseDialogReflectiveProxy<D> {
    public ManifestDialogReflectiveProxy (Class<D> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected ManifestDialogReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /* Ignore any children
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
     */
    @Override
    public D fromXmlChild (D src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        // ignore columns specs which we know might follow the manifest
        if (EnumColumnAbstractTableModel.COLUMN_ELEM_NAME.equalsIgnoreCase(tagName)
        // ignore ready-made entries
         || ManifestTableModel.PREDEFINED_MANIFEST_ENTRY_ELEM_NAME.equalsIgnoreCase(tagName))
            return src;

        return super.fromXmlChild(src, elem);
    }

    public static final ManifestDialogReflectiveProxy<ManifestDialog>    MANIFEST=
                new ManifestDialogReflectiveProxy<ManifestDialog>(ManifestDialog.class, true);
}
