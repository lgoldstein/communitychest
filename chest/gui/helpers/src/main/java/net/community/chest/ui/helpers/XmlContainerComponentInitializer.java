/*
 *
 */
package net.community.chest.ui.helpers;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Also provides "sections" definitions sub-{@link Element}-s</P>
 * @author Lyor G.
 * @since Dec 11, 2008 3:30:26 PM
 */
public interface XmlContainerComponentInitializer extends XmlElementComponentInitializer {
    /**
     * @return A {@link java.util.Map} of sections (may be null/empty) - key=section name,
     * value=section definition {@link Element}
     */
    SectionsMap getSectionsMap ();
    // CAVEAT EMPTOR may be too late if called after "layoutSection"
    void setSectionsMap (SectionsMap sectionsMap);
    /**
     * @param name Name of section to be added (ignored if null/empty)
     * @param elem Section definition {@link Element} (ignored if null)
     * @return Previous definition (null if none)
     */
    // CAVEAT EMPTOR may be too late if called after "layoutSection"
    Element addSection (String name, Element elem);
    /**
     * @param name Section name in the sections map
     * @return The section XML {@link Element} (<code>null</code> if none)
     * @see #getSectionsMap()
     */
    Element getSection (String name);
    /**
     * Invoked by {@link #getSection(Enum)} in order to resolve the section
     * name
     * @param <E> The {@link Enum} type being used
     * @param v The value
     * @return The {@link String} to use for {@link #getSection(String)} call.
     * Default={@link Enum#toString()}
     */
    <E extends Enum<E>> String getSectionName (E v);
    <E extends Enum<E>> Element getSection (E v);
    /**
     * @param name Section name
     * @param elem Section XML {@link Element}
     * @throws RuntimeException If failed to layout the section
     */
    void layoutSection (String name, Element elem) throws RuntimeException;
}
