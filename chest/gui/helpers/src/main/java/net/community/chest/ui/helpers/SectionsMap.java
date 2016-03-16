/*
 *
 */
package net.community.chest.ui.helpers;

import java.util.Collection;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 19, 2009 1:57:20 PM
 */
public interface SectionsMap extends Map<String,Element> {
    /**
     * @return A {@link Collection} of the names of sections in the
     * <U>same</U> order as the way the {@link #put(String, Element)}
     * was called (if same element re-mapped, then 1st call is used)
     */
    Collection<String> getSectionsNames ();
    /**
     * @return A {@link Collection} of "pairs" represented as
     * {@link java.util.Map.Entry}-ies where key=section name, value=section XML
     * {@link Element}. The <U>order</U> of these pairs is according to the
     * one specified in {@link #getSectionsNames()} (which may differ from
     * the {@link #entrySet()}
     */
    Collection<Map.Entry<String,Element>> sectionsSet ();
}
