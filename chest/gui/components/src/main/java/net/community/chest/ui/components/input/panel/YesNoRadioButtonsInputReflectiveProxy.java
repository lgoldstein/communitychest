/*
 *
 */
package net.community.chest.ui.components.input.panel;

import javax.swing.AbstractButton;

import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.component.button.AbstractButtonReflectiveProxy;
import net.community.chest.swing.component.button.JRadioButtonReflectiveProxy;
import net.community.chest.ui.helpers.panel.HelperPanelReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> Type of {@link YesNoRadioButtonsInput} being reflected
 * @author Lyor G.
 * @since Jan 15, 2009 2:10:46 PM
 */
public class YesNoRadioButtonsInputReflectiveProxy<P extends YesNoRadioButtonsInput>
        extends HelperPanelReflectiveProxy<P> {
    protected YesNoRadioButtonsInputReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public YesNoRadioButtonsInputReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public boolean isButtonElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, AbstractButtonReflectiveProxy.BUTTON_ELEMNAME);
    }

    public XmlProxyConvertible<? extends AbstractButton> getButtonConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : JRadioButtonReflectiveProxy.RADIO;
    }

    public AbstractButton addButton (P src, Element elem) throws Exception
    {
        final String            btnType=elem.getAttribute(CLASS_ATTR);
        final boolean            optVal=Boolean.parseBoolean(btnType);
        final AbstractButton    orgBtn=src.getButton(optVal),
                                b=(null == orgBtn) ? src.createButton(optVal) : orgBtn;
        if (b != null)
        {
            final XmlProxyConvertible<? extends AbstractButton>    proxy=getButtonConverter(elem);
            if (proxy != null)
            {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                final Object    v=((XmlProxyConvertible) proxy).fromXml(b, elem);
                if (b != v)
                    throw new IllegalStateException("addButton(" + btnType + ") mismatched instances");
            }

            if (null == orgBtn)
            {
                final AbstractButton    prev=src.addButton(optVal, b);
                if (prev != null)
                    throw new IllegalStateException("addButton(" + btnType + ") already initialized");
            }
        }

        return b;
    }
    /*
     * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isButtonElement(elem, tagName))
        {
            addButton(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final YesNoRadioButtonsInputReflectiveProxy<YesNoRadioButtonsInput>    YESNOINP=
        new YesNoRadioButtonsInputReflectiveProxy<YesNoRadioButtonsInput>(YesNoRadioButtonsInput.class, true);
}
