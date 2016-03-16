/*
 *
 */
package net.community.chest.ui.helpers;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Interface implemented by various useful components that use an XML
 * {@link Document} to initialize themselves - e.g., panel, dialog as
 * well as an element</P>
 * @author Lyor G.
 * @since Oct 30, 2008 11:08:53 AM
 */
public interface XmlDocumentComponentInitializer extends XmlContainerComponentInitializer {
    /**
     * @return The XML {@link Document} to be used to initialize the component
     * (may be null)
     * @throws RuntimeException If failed to load the document
     */
    Document getComponentDocument () throws RuntimeException;
    // CAVEAT EMPTOR: if you call it AFTER layoutComponent it may be too late...
    void setComponentDocument (Document doc);
    /**
     * @param doc The XML {@link Document} to be used to initialize the component
     * (may be null)
     * @throws RuntimeException If failed to initialize the document
     */
    void layoutComponent (Document doc) throws RuntimeException;
}
