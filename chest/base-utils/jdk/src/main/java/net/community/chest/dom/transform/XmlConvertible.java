package net.community.chest.dom.transform;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Copyright 2007 as per GPLv2
 *
 * Used to indicate the fact that an object is "serialize-able" as an XML.
 *
 * @param <T> Type of converted object
 * @author Lyor G.
 * @since Jun 19, 2007 11:54:04 AM
 */
public interface XmlConvertible<T> {
    /**
     * @param doc {@link Document} to be used for creating elements.
     * <B>Caveat:</B> the {@link Document} instance should only be used to
     * create new {@link Element}-s and nothing else. Any "side-effects"
     * that are the result of manipulating the {@link Document} are undefined
     * @return root {@link Element} of the sub-tree that represents the
     * object's current state/data
     * @throws Exception if unable to create sub-tree
     */
    Element toXml (Document doc) throws Exception;
    /**
     * @param root sub-tree root {@link Element} under which the object's XML
     * "encoding" resides - usually same as returned by {@link #toXml(Document)}
     * call
     * @return initialized object - usually same as <code>this</code>
     * @throws Exception if unable to "de-serialize" the object from its XML
     */
    T fromXml (Element root) throws Exception;
}
