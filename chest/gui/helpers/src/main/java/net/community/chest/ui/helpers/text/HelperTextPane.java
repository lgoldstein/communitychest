/*
 *
 */
package net.community.chest.ui.helpers.text;

import javax.swing.text.StyledDocument;

import org.w3c.dom.Element;

import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.text.BaseTextPane;
import net.community.chest.ui.helpers.XmlElementComponentInitializer;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 2, 2009 10:03:10 AM
 */
public class HelperTextPane extends BaseTextPane implements XmlElementComponentInitializer  {
    /**
     *
     */
    private static final long serialVersionUID = -4871254160239174851L;
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

    public HelperTextPane (Element elem, boolean autoLayout)
    {
        setComponentElement(elem);
        if (autoLayout)
            layoutComponent();
    }

    public HelperTextPane (Element elem)
    {
        this(elem, true);
    }

    public HelperTextPane (boolean autoLayout)
    {
        this(null, autoLayout);
    }

    public HelperTextPane ()
    {
        this(true);
    }

    public HelperTextPane (StyledDocument doc)
    {
        super(doc);
    }
}
