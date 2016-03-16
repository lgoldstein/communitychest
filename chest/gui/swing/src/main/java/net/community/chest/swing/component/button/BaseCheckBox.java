/*
 *
 */
package net.community.chest.swing.component.button;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;

import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.FontControl;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Selectible;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.awt.attributes.Tooltiped;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.swing.component.menu.BaseMenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 21, 2008 3:49:28 PM
 */
public class BaseCheckBox extends JCheckBox
        implements XmlConvertible<BaseCheckBox>,
                   Textable, Selectible, Iconable, FontControl,
                   Tooltiped, Enabled, Foregrounded, Backgrounded {
    /**
     *
     */
    private static final long serialVersionUID = 7433599607384004545L;
    public BaseCheckBox ()
    {
        super();
    }

    public BaseCheckBox (Icon icon)
    {
        super(icon);
    }

    public BaseCheckBox (String text)
    {
        super(text);
    }

    public BaseCheckBox (Action a)
    {
        super(a);
    }

    public BaseCheckBox (Icon icon, boolean selected)
    {
        super(icon, selected);
    }

    public BaseCheckBox (String text, boolean selected)
    {
        super(text, selected);
    }

    public BaseCheckBox (String text, Icon icon)
    {
        super(text, icon);
    }

    public BaseCheckBox (String text, Icon icon, boolean selected)
    {
        super(text, icon, selected);
    }

    public XmlProxyConvertible<?> getCheckBoxConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : JCheckBoxReflectiveProxy.CB;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseCheckBox fromXml (final Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getCheckBoxConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "fromXml", DOMUtils.toString(elem)) + " mismatched re-constructed instances");

        return this;
    }

    public BaseCheckBox  (Element elem) throws Exception
    {
        final BaseCheckBox    item=fromXml(elem);
        if (item != this)    // not allowed
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched restored " + BaseMenuItem.class.getName() + " instances");
    }
    /**
     * Preferred name of an XML {@link Element} for this type
     */
    public static final String    CHECKBOX_ELEM_NAME="checkbox";
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        // TODO implement toXml
        throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
    }
}
