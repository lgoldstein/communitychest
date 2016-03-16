/*
 *
 */
package net.community.chest.swing.component.tabbed;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 23, 2008 8:55:22 AM
 */
public class BaseTabbedPane extends JTabbedPane implements XmlConvertible<BaseTabbedPane> {
    /**
     *
     */
    private static final long serialVersionUID = 5927184296534055538L;
    public BaseTabbedPane ()
    {
        super();
    }

    public BaseTabbedPane (int placement)
    {
        super(placement);
    }

    public BaseTabbedPane (TabPlacement p)
    {
        this(p.getPlacement());
    }

    public BaseTabbedPane (int placement, int layoutPolicy)
    {
        super(placement, layoutPolicy);
    }

    public BaseTabbedPane (TabPlacement p, TabLayoutPolicy l)
    {
        this(p.getPlacement(), l.getPolicy());
    }

    protected XmlProxyConvertible<?> getPaneConverter (final Element elem)
    {
        return (null == elem) ? null : JTabbedPaneReflectiveProxy.TABBED;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseTabbedPane fromXml (Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getPaneConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    co=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (co != this)
            throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched initialization instances");

        return this;
    }

    public BaseTabbedPane (Element elem) throws Exception
    {
        final Object    p=fromXml(elem);
        if (p != this)    // not allowed
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched restored " + JPanel.class.getName() + " instances");
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
