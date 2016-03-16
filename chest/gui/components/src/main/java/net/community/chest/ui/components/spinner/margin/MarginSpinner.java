/*
 *
 */
package net.community.chest.ui.components.spinner.margin;

import javax.swing.SpinnerModel;

import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.component.spinner.IntegerSpinnerNumberModel;
import net.community.chest.ui.helpers.spinner.TypedSpinner;

import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 11, 2009 11:29:43 AM
 *
 */
public class MarginSpinner extends TypedSpinner<BorderLayoutPosition> {
    /**
     *
     */
    private static final long serialVersionUID = 6973866398419849658L;

    public BorderLayoutPosition getPosition ()
    {
        return getAssignedValue();
    }

    public void setPosition (BorderLayoutPosition p)
    {
        setAssignedValue(p);
    }

    public MarginSpinner (SpinnerModel m, BorderLayoutPosition p, Element elem, boolean autoLayout)
    {
        super(BorderLayoutPosition.class, p, m, elem, autoLayout);
    }

    public MarginSpinner (SpinnerModel m, BorderLayoutPosition p, Element elem)
    {
        this(m, p, elem, true);
    }

    public MarginSpinner (SpinnerModel m, BorderLayoutPosition p, boolean autoLayout)
    {
        this(m, p, null, autoLayout);
    }

    public MarginSpinner (SpinnerModel m, BorderLayoutPosition p)
    {
        this(m, p, true);
    }

    public MarginSpinner (SpinnerModel m, boolean autoLayout)
    {
        this(m, null, autoLayout);
    }

    public MarginSpinner (SpinnerModel m)
    {
        this(m, true);
    }

    public MarginSpinner (BorderLayoutPosition p, boolean autoLayout)
    {
        this(new IntegerSpinnerNumberModel(), p, autoLayout);
    }

    public MarginSpinner (BorderLayoutPosition p)
    {
        this(p, true);
    }

    public MarginSpinner (boolean autoLayout)
    {
        this((BorderLayoutPosition) null, autoLayout);
    }

    public MarginSpinner ()
    {
        this(true);
    }
    /*
     * @see net.community.chest.swing.component.spinner.BaseSpinner#getSpinnerConverter(org.w3c.dom.Element)
     */
    @Override
    protected XmlProxyConvertible<?> getSpinnerConverter (Element elem)
    {
        return (null == elem) ? null : MarginSpinnerReflectiveProxy.MRGNSPIN;
    }

    public MarginSpinner (Element elem) throws Exception
    {
        this();

        final Object    o=fromXml(elem);
        if (o != this)
            throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched instances");
    }
}
