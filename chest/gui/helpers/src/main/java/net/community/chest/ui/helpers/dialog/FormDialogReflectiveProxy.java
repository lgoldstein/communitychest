/*
 *
 */
package net.community.chest.ui.helpers.dialog;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> The reflected {@link FormDialog}
 * @author Lyor G.
 * @since Jan 6, 2009 3:16:15 PM
 */
public class FormDialogReflectiveProxy<D extends FormDialog> extends HelperDialogReflectiveProxy<D> {
    protected FormDialogReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public FormDialogReflectiveProxy (Class<D> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public boolean isButtonsPanelElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, ButtonsPanel.class.getSimpleName());
    }

    public XmlProxyConvertible<? extends ButtonsPanel> getButtonsPanelConverter (Element elem)
    {
        return (null == elem) ? null : ButtonsPanelReflectiveProxy.BTNSPNL;
    }

    public ButtonsPanel layoutButtonsPanel (D src, Element elem) throws Exception
    {
        final XmlProxyConvertible<? extends ButtonsPanel>    conv=getButtonsPanelConverter(elem);
        @SuppressWarnings("unchecked")
        final ButtonsPanel                                    org=src.getButtonsPanel(),
                                                            p=
            (null == org) ? conv.fromXml(elem) : ((XmlProxyConvertible<ButtonsPanel>) conv).fromXml(org, elem);
        if (p != null)
        {
            if (null == org)
                src.setButtonsPanel(p);
            else if (p != org)
                throw new IllegalStateException("layoutButtonsPanel(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
        }

        return p;
    }
    /*
     * @see net.community.chest.swing.component.dialog.JDialogReflectiveProxy#fromXmlChild(javax.swing.JDialog, org.w3c.dom.Element)
     */
    @Override
    public D fromXmlChild (D src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isButtonsPanelElement(elem, tagName))
        {
            layoutButtonsPanel(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final FormDialogReflectiveProxy<FormDialog>    FRMDLG=
            new FormDialogReflectiveProxy<FormDialog>(FormDialog.class, true);
}
