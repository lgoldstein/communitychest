package net.community.chest.swing.component.menu;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JRadioButtonMenuItem;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 12:23:07 PM
 */
public class BaseRadioButtonMenuItem extends JRadioButtonMenuItem implements XmlConvertible<BaseRadioButtonMenuItem> {
    /**
     *
     */
    private static final long serialVersionUID = -4619749923809455288L;
    public BaseRadioButtonMenuItem ()
    {
        super();
    }

    public BaseRadioButtonMenuItem (Icon icon)
    {
        super(icon);
    }

    public BaseRadioButtonMenuItem (String text)
    {
        super(text);
    }

    public BaseRadioButtonMenuItem (Action a)
    {
        super(a);
    }

    public BaseRadioButtonMenuItem (String text, Icon icon)
    {
        super(text, icon);
    }

    public BaseRadioButtonMenuItem (String text, boolean selected)
    {
        super(text, selected);
    }

    public BaseRadioButtonMenuItem (Icon icon, boolean selected)
    {
        super(icon, selected);
    }

    public BaseRadioButtonMenuItem (String text, Icon icon, boolean selected)
    {
        super(text, icon, selected);
    }

    public XmlProxyConvertible<?> getRadioButtonMenuItemConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : JRadioButtonMenuItemReflectiveProxy.RADIOMENUITEM;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseRadioButtonMenuItem fromXml (final Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getRadioButtonMenuItemConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "fromXml", DOMUtils.toString(elem)) + " mismatched re-constructed instances");

        return this;
    }

    public BaseRadioButtonMenuItem (Element elem) throws Exception
    {
        final BaseRadioButtonMenuItem    item=fromXml(elem);
        if (item != this)    // not allowed
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched restored " + BaseMenuItem.class.getName() + " instances");
    }
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
