/*
 *
 */
package net.community.chest.ui.helpers.panel.input;

import javax.swing.JFileChooser;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.component.filechooser.JFileChooserReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link FileInputTextPanel} instance
 *
 * @author Lyor G.
 * @since Dec 30, 2008 12:56:12 PM
 */
public class FileInputTextPanelReflectiveProxy<P extends FileInputTextPanel> extends LRFieldWithButtonReflectiveProxy<P> {
    public FileInputTextPanelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected FileInputTextPanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public boolean isFileChooserElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, JFileChooserReflectiveProxy.CHOOSER_ELEMNAME);
    }

    public XmlProxyConvertible<?> getFileChooserConverter (Element elem) throws Exception
    {
        return (null == elem) ? null : JFileChooserReflectiveProxy.CHOOSER;
    }

    public JFileChooser setFileChooser (P src, Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getFileChooserConverter(elem);
        final JFileChooser                orgCh=src.getFileChooser();
        @SuppressWarnings("unchecked")
        final Object                    o=
            (null == orgCh) ? proxy.fromXml(elem) : ((XmlProxyConvertible<Object>) proxy).fromXml(orgCh, elem);
        if (null == o)
            return null;
        if (!(o instanceof JFileChooser))
            throw new ClassCastException("setFileChooser() bad class (" + o.getClass().getName() + ") for element=" + DOMUtils.toString(elem));

        final JFileChooser    fc=(JFileChooser) o;
        if (null == orgCh)
            src.setFileChooser(fc);
        else if (fc != orgCh)
            throw new IllegalStateException("setFileChooser(" + DOMUtils.toString(elem) + ") mismatched instances");

        return fc;
    }
    /*
     * @see net.community.chest.ui.helpers.panel.input.FieldWithButtonPanelReflectiveProxy#fromXmlChild(net.community.chest.ui.helpers.panel.input.AbstractFieldWithButtonPanel, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=(null == elem) ? null : elem.getTagName();
        if (isFileChooserElement(elem, tagName))
        {
            setFileChooser(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final FileInputTextPanelReflectiveProxy<FileInputTextPanel>    FILEINPTXTPNL=
        new FileInputTextPanelReflectiveProxy<FileInputTextPanel>(FileInputTextPanel.class, true);
}
