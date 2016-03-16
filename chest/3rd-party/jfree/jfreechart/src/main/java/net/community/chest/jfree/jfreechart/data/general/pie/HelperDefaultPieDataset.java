/*
 *
 */
package net.community.chest.jfree.jfreechart.data.general.pie;

import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.ui.helpers.XmlElementComponentInitializer;

import org.jfree.data.KeyedValues;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 1, 2009 2:12:46 PM
 */
public class HelperDefaultPieDataset extends BaseDefaultPieDataset implements XmlElementComponentInitializer {
    /**
     *
     */
    private static final long serialVersionUID = 4036479347339716220L;
    public HelperDefaultPieDataset (KeyedValues data)
    {
        super(data);
    }

    private Element    _elem;
    /*
     * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#getComponentElement()
     */
    @Override
    public Element getComponentElement () throws RuntimeException
    {
        return _elem;
    }
    /*
     * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#setComponentElement(org.w3c.dom.Element)
     */
    @Override
    public void setComponentElement (Element elem)
    {
        if (_elem != elem)
            _elem = elem;
    }
    /*
     * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#layoutComponent(org.w3c.dom.Element)
     */
    @Override
    public void layoutComponent (Element elem) throws RuntimeException
    {
        if (elem != null)
        {
            try
            {
                final Object    o=fromXml(elem);
                if (this != o)
                    throw new IllegalStateException("layoutComponent(" + DOMUtils.toString(elem) + ") mismatched instances");
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }

            setComponentElement(elem);
        }
    }
    /*
     * @see net.community.chest.ui.helpers.ComponentInitializer#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        layoutComponent(getComponentElement());
    }

    public HelperDefaultPieDataset (Element elem, boolean autoInit) throws RuntimeException
    {
        setComponentElement(elem);
        if (autoInit)
            layoutComponent();
    }

    public HelperDefaultPieDataset (Element elem) throws RuntimeException
    {
        this(elem, true);
    }

    public HelperDefaultPieDataset (boolean autoInit)
    {
        this(null, autoInit);
    }

    public HelperDefaultPieDataset ()
    {
        this(true);
    }

}
