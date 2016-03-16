/*
 *
 */
package net.community.chest.ui.helpers;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Interface implemented by various useful components that use an XML
 * {@link Element} to initialize themselves - e.g., panel, dialog</P>
 *
 * @author Lyor G.
 * @since Oct 30, 2008 11:06:52 AM
 */
public interface XmlElementComponentInitializer extends ComponentInitializer {
    /**
     * @return The root XML {@link Element} to be used to initialize the
     * component - may be null
     * @throws RuntimeException If failed to load the component element
     */
    Element getComponentElement () throws RuntimeException;
    // CAVEAT EMPTOR if you call it AFTER layoutComponent it may be too late...
    void setComponentElement (final Element elem);
    /**
     * @param elem The root XML {@link Element} to be used to initialize the
     * component - may be null
     * @throws RuntimeException If failed to layout the component
     */
    void layoutComponent (Element elem) throws RuntimeException;
}
