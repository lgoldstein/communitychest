/*
 *
 */
package net.community.chest.swing.component.panel;

import java.awt.Component;

import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.dom.transform.XmlValueInstantiator;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Specialized reflective XML proxy for panels that use a {@link java.awt.BorderLayout}</P>
 *
 * @param <P> The {@link javax.swing.JPanel} being reflected
 * @author Lyor G.
 * @since Aug 25, 2008 12:41:25 PM
 */
public abstract class BorderLayoutPanelReflectiveProxy<P extends BasePanel> extends BasePanelReflectiveProxy<P> {
    protected BorderLayoutPanelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected BorderLayoutPanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /**
     * Called by default implementation of <code>addComponent</code>
     * in order to retrieve the {@link XmlValueInstantiator} instance that can be
     * used to generate the {@link Component} to be added to the {@link javax.swing.JPanel}
     * at the specified {@link java.awt.BorderLayout} position
     * @param elem The XML element containing the {@link Component}'s data
     * @return The {@link XmlValueInstantiator} for instantiating the component
     * @throws Exception If cannot resolve the {@link XmlValueInstantiator} instance
     */
    public abstract XmlValueInstantiator<? extends Component> getComponentConverter (final Element elem) throws Exception;

    protected Component addComponent (final P src, final Element elem, final BorderLayoutPosition pos) throws Exception
    {
        final XmlValueInstantiator<? extends Component>    conv=getComponentConverter(elem);
        final Component                                    comp=conv.fromXml(elem);
        if (comp != null)
            src.add(comp, pos.getPosition());
        return comp;
    }
    /**
     * Called by default implementation of {@link #fromXmlChild(BasePanel, Element)}
     * in order to determine if the {@link Element} refers to a {@link Component}.
     * By default, it checks if the element name is one of the {@link BorderLayoutPosition}
     * values <U>case insensitive</U>
     * @param elem The XML {@link Element}
     * @param tagName The element tag name
     * @return The matching {@link BorderLayoutPosition} - null if this is not
     * a component position
     */
    public BorderLayoutPosition getComponentPosition (final Element elem, final String tagName)
    {
        return (null == elem) ? null : BorderLayoutPosition.fromString(tagName);
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String                tagName=elem.getTagName();
        final BorderLayoutPosition    pos=getComponentPosition(elem, tagName);
        if (pos != null)
        {
            addComponent(src, elem, pos);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }
}
