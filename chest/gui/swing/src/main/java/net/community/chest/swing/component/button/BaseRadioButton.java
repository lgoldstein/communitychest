/*
 *
 */
package net.community.chest.swing.component.button;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JRadioButton;

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
 * @since Aug 28, 2008 11:26:16 AM
 */
public class BaseRadioButton extends JRadioButton implements XmlConvertible<BaseRadioButton> {
    /**
     *
     */
    private static final long serialVersionUID = -9032120259207777324L;
    public BaseRadioButton ()
    {
        super();
    }

    public BaseRadioButton (Icon icon)
    {
        super(icon);
    }

    public BaseRadioButton (Action a)
    {
        super(a);
    }

    public BaseRadioButton (String text)
    {
        super(text);
    }

    public BaseRadioButton (Icon icon, boolean selected)
    {
        super(icon, selected);
    }

    public BaseRadioButton (String text, boolean selected)
    {
        super(text, selected);
    }

    public BaseRadioButton (String text, Icon icon)
    {
        super(text, icon);
    }

    public BaseRadioButton (String text, Icon icon, boolean selected)
    {
        super(text, icon, selected);
    }

    protected XmlProxyConvertible<?> getButtonConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : JRadioButtonReflectiveProxy.RADIO;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseRadioButton fromXml (Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getButtonConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + " mismatched initialization instances");

        return this;
    }

    public BaseRadioButton (Element elem) throws Exception
    {
        if (fromXml(elem) != this)
            throw new IllegalStateException("<init>" + DOMUtils.toString(elem) + ") mismatched restored " + JMenu.class.getName() + " instances");
    }
    /**
     * Default element name used for button(s)
     */
    public static final String RADIO_ELEMNAME="radio";
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
