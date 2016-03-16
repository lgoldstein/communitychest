/*
 *
 */
package net.community.chest.swing.component.spinner;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 11, 2009 11:26:46 AM
 *
 */
public class BaseSpinner extends JSpinner implements XmlConvertible<BaseSpinner> {
    /**
     *
     */
    private static final long serialVersionUID = 9108163768497244843L;
    public BaseSpinner (SpinnerModel model)
    {
        super(model);
    }

    public BaseSpinner ()
    {
        this(new BaseSpinnerNumberModel());
    }

    protected XmlProxyConvertible<?> getSpinnerConverter (Element elem)
    {
        return (null == elem) ? null : JSpinnerReflectiveProxy.SPINNER;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseSpinner fromXml (Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getSpinnerConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            (null == proxy) ? this : ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched instances");
        return this;
    }

    public BaseSpinner (Element elem) throws Exception
    {
        final Object    o=fromXml(elem);
        if (o != this)
            throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched instances");
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
