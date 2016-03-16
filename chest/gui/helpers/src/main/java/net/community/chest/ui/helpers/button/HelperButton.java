/*
 *
 */
package net.community.chest.ui.helpers.button;

import javax.swing.Icon;

import org.w3c.dom.Element;

import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.button.BaseButton;
import net.community.chest.ui.helpers.XmlElementComponentInitializer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 19, 2009 1:01:52 PM
 */
public class HelperButton extends BaseButton implements XmlElementComponentInitializer {
    /**
     *
     */
    private static final long serialVersionUID = 3167892439978936545L;
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

    public HelperButton (String text, Icon icon, Element elem, boolean autoLayout)
    {
        super(text, icon);

        setComponentElement(elem);
        if (autoLayout)
            layoutComponent();
    }

    public HelperButton (String text, Icon icon, Element elem)
    {
        this(text, icon, elem, true);
    }

    public HelperButton (String text, Icon icon, boolean autoLayout)
    {
        this(text, icon, null, autoLayout);
    }

    public HelperButton (String text, Icon icon)
    {
        this(text, icon, true);
    }

    public HelperButton (String text, boolean autoLayout)
    {
        this(text, null, autoLayout);
    }

    public HelperButton (String text)
    {
        this(text, true);
    }

    public HelperButton (Element elem, boolean autoLayout)
    {
        this(null, null, elem, autoLayout);
    }

    public HelperButton (Element elem)
    {
        this(elem, true);
    }

    public HelperButton (boolean autoLayout)
    {
        this((Element) null, autoLayout);
    }

    public HelperButton ()
    {
        this(true);
    }
}
