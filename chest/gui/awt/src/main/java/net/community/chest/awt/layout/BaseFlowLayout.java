package net.community.chest.awt.layout;

import java.awt.FlowLayout;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.layout.dom.FlowLayoutReflectiveProxy;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 19, 2008 1:13:56 PM
 */
public class BaseFlowLayout extends FlowLayout
        implements XmlConvertible<BaseFlowLayout>, PubliclyCloneable<BaseFlowLayout> {

    /**
     *
     */
    private static final long serialVersionUID = -6860634581307922492L;
    public BaseFlowLayout ()
    {
        super();
    }

    public BaseFlowLayout (FlowLayoutAlignment align)
    {
        super(align.getAlignment());
    }

    public BaseFlowLayout (FlowLayoutAlignment align, int hgap, int vgap)
    {
        super(align.getAlignment(), hgap, vgap);
    }

    public FlowLayoutAlignment getLayoutAlignment ()
    {
        return FlowLayoutAlignment.fromAlignment(getAlignment());
    }

    public void setLayoutAlignment (FlowLayoutAlignment align)
    {
        setAlignment(align.getAlignment());
    }

    public boolean isAlignOnBaseline ()
    {
        return getAlignOnBaseline();
    }


    public XmlProxyConvertible<?> getLayoutConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : FlowLayoutReflectiveProxy.FLOW;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseFlowLayout fromXml (final Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getLayoutConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched initialization instances");

        return this;
    }

    public BaseFlowLayout (final Element elem) throws Exception
    {
        if (fromXml(elem) != this)
            throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched constructed instances");
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
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public BaseFlowLayout clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}
