package net.community.chest.swing.component.menu;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.FontControl;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.awt.attributes.Tooltiped;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 24, 2007 1:54:10 PM
 */
public class BaseMenuItem extends JMenuItem
        implements XmlConvertible<BaseMenuItem>,
            Textable, Iconable, FontControl, Tooltiped, Enabled, Foregrounded, Backgrounded {
    /**
     *
     */
    private static final long serialVersionUID = 7886494608662335268L;
    public BaseMenuItem ()
    {
        super();
    }

    public BaseMenuItem (Icon icon)
    {
        super(icon);
    }

    public BaseMenuItem (String text)
    {
        super(text);
    }

    public BaseMenuItem (Action a)
    {
        super(a);
    }

    public BaseMenuItem (String text, Icon icon)
    {
        super(text, icon);
    }

    public BaseMenuItem (String text, int mnemonic)
    {
        super(text, mnemonic);
    }

    protected XmlProxyConvertible<?> getMenuItemConverter (final Element elem)
    {
        return (null == elem) ? null : JMenuItemReflectiveProxy.MENUITEM;
    }
    /* NOTE(s):
     * - does not check that XML element name is as expected
     * - does not "clear" the current contents - only overrides them
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseMenuItem fromXml (final Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getMenuItemConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched re-constructed instances");

        return this;
    }

    public BaseMenuItem (final Element elem) throws Exception
    {
        final JMenuItem    item=fromXml(elem);
        if (item != this)    // not allowed
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched restored " + JMenuItem.class.getName() + " instances");
    }

    public static final String    ITEM_ELEMNAME="item";
    /*
     * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        // TODO implement toXml
        throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
    }
}
