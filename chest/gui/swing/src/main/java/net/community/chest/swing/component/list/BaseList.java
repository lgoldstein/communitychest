/*
 *
 */
package net.community.chest.swing.component.list;

import java.util.Vector;

import javax.swing.JList;
import javax.swing.ListModel;

import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Tooltiped;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 7, 2010 3:11:13 PM
 */
public class BaseList extends JList
            implements XmlConvertible<BaseList>,
                      Backgrounded, Foregrounded, Enabled, Tooltiped {
    /**
     *
     */
    private static final long serialVersionUID = 7543820705937419034L;
    public BaseList ()
    {
        super();
    }

    public BaseList (ListModel dataModel)
    {
        super(dataModel);
    }

    public BaseList (Object ... listData)
    {
        super(listData);
    }

    public BaseList (Vector<?> listData)
    {
        super(listData);
    }


    public XmlProxyConvertible<?> getListConverter (Element elem)
    {
        return (null == elem) ? null : JListReflectiveProxy.LIST;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseList fromXml (Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getListConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
        return this;
    }

    public BaseList (Element elem) throws Exception
    {
        final Object    o=fromXml(elem);
        if (o != this)
            throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        throw new UnsupportedOperationException("toXml() N/A");
    }
}
