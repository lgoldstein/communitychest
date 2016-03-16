/*
 *
 */
package net.community.chest.ui.helpers.text;

import javax.swing.text.Document;

import org.w3c.dom.Element;

import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.text.BaseTextArea;
import net.community.chest.ui.helpers.XmlElementComponentInitializer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 22, 2009 11:08:10 AM
 */
public class HelperTextArea extends BaseTextArea implements XmlElementComponentInitializer  {
    /**
     *
     */
    private static final long serialVersionUID = 522391530416816220L;
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

    public HelperTextArea (Document doc, String text, int rows, int columns)
    {
        super(doc, text, rows, columns);
    }

    public HelperTextArea (String text, int rows, int columns)
    {
        this(null, text, rows, columns);
    }

    public HelperTextArea (String text)
    {
        this(null, text, 0, 0);
    }

    public HelperTextArea ()
    {
        this((String) null);
    }

    public HelperTextArea (int rows, int columns)
    {
        this(null, rows, columns);
    }

    public HelperTextArea (Document doc)
    {
        this(doc, null, 0, 0);
    }

    public HelperTextArea (Element elem, boolean autoLayout)
    {
        setComponentElement(elem);

        if (autoLayout)
            layoutComponent();
    }

    public HelperTextArea (Element elem)
    {
        this(elem, true);
    }
}
