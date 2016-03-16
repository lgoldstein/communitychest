/*
 *
 */
package net.community.chest.ui.helpers.dialog;

import net.community.chest.dom.DOMUtils;
import net.community.chest.swing.component.dialog.BaseDialogReflectiveProxy;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> The reflected {@link HelperDialog} instance
 * @author Lyor G.
 * @since Dec 11, 2008 3:44:58 PM
 */
public class HelperDialogReflectiveProxy<D extends HelperDialog> extends BaseDialogReflectiveProxy<D> {
    public HelperDialogReflectiveProxy (Class<D> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected HelperDialogReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public String getSectionName (D src, Element elem)
    {
        if ((null == src) || (null == elem))
            return null;

        return elem.getAttribute(NAME_ATTR);
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#handleUnknownXmlChild(java.lang.Object, org.w3c.dom.Element)
     */
    @Override
    public D handleUnknownXmlChild (D src, Element elem) throws Exception
    {
        final String    n=getSectionName(src, elem);
        if ((n != null) && (n.length() > 0))
        {
            final Element    prev=src.addSection(n, elem);
            if (prev != null)
                throw new IllegalStateException("handleUnknownXmlChild(" + n + "[" + DOMUtils.toString(elem) + "] duplicate section found: " + DOMUtils.toString(prev));

            return src;
        }

        return super.handleUnknownXmlChild(src, elem);
    }

    public static final HelperDialogReflectiveProxy<HelperDialog>    HLPRDLG=
        new HelperDialogReflectiveProxy<HelperDialog>(HelperDialog.class, true) {
            /* Need to override this in order to ensure correct auto-layout
             * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
             */
            @Override
            public HelperDialog fromXml (Element elem) throws Exception
            {
                return (null == elem) ? null : new HelperDialog(elem);
            }
        };
}
