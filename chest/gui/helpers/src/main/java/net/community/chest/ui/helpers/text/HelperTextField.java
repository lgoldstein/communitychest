/*
 *
 */
package net.community.chest.ui.helpers.text;

import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.text.BaseTextField;
import net.community.chest.ui.helpers.XmlElementComponentInitializer;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 12, 2009 11:29:39 AM
 */
public class HelperTextField extends BaseTextField implements XmlElementComponentInitializer  {
    /**
     *
     */
    private static final long serialVersionUID = -1590168310777917095L;
    private    Element    _cElem    /* =null */;
    /*
     * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#getComponentElement()
     */
    @Override
    public Element getComponentElement () throws RuntimeException
    {
        return _cElem;
    }
    /*
     * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#setComponentElement(org.w3c.dom.Element)
     */
    @Override
    public void setComponentElement (Element elem)
    {
        if (_cElem != elem)
            _cElem = elem;
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
                if (fromXml(elem) != this)
                    throw new IllegalStateException("layoutComponent(" + DOMUtils.toString(elem) + ") mismatched re-constructed instance");
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

    public HelperTextField (String text, int columns, Element elem, boolean autoLayout)
    {
        super(text, columns);

        setComponentElement(elem);
        if (autoLayout)
            layoutComponent();
    }

    public HelperTextField (String text, Element elem, boolean autoLayout)
    {
        this(text, 0, elem, autoLayout);
    }

    public HelperTextField (String text, Element elem)
    {
        this(text, elem, true);
    }

    public HelperTextField (Element elem, boolean autoLayout)
    {
        this(null, elem, autoLayout);
    }

    public HelperTextField (Element elem)
    {
        this(elem, true);
    }

    public HelperTextField (String text, int columns, boolean autoLayout)
    {
        this(text, columns, null, autoLayout);
    }

    public HelperTextField (String text, int columns)
    {
        this(text, columns, true);
    }

    public HelperTextField (String text, boolean autoLayout)
    {
        this(text, 0, autoLayout);
    }

    public HelperTextField (String text)
    {
        this(text, true);
    }

    public HelperTextField (int columns)
    {
        this(null, columns);
    }

    public HelperTextField (boolean autoLayout)
    {
        this((Element) null, autoLayout);
    }

    public HelperTextField ()
    {
        this(true);
    }
}
